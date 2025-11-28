package net.invictusmanagement.invictuskiosk.presentation.video_call

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twilio.video.Camera2Capturer
import com.twilio.video.ConnectOptions
import com.twilio.video.LocalAudioTrack
import com.twilio.video.LocalVideoTrack
import com.twilio.video.RemoteAudioTrack
import com.twilio.video.RemoteAudioTrackPublication
import com.twilio.video.RemoteDataTrack
import com.twilio.video.RemoteDataTrackPublication
import com.twilio.video.RemoteParticipant
import com.twilio.video.RemoteVideoTrack
import com.twilio.video.RemoteVideoTrackPublication
import com.twilio.video.Room
import com.twilio.video.TwilioException
import com.twilio.video.Video
import com.twilio.video.VideoCapturer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import net.invictusmanagement.invictuskiosk.commons.Constants
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.dto.MissedCallDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.VideoCallDto
import net.invictusmanagement.invictuskiosk.domain.model.VideoCallToken
import net.invictusmanagement.invictuskiosk.domain.repository.RelayManagerRepository
import net.invictusmanagement.invictuskiosk.domain.repository.VideoCallRepository
import net.invictusmanagement.invictuskiosk.domain.repository.ScreenSaverRepository
import net.invictusmanagement.invictuskiosk.presentation.signalR.ChatHubManager
import net.invictusmanagement.invictuskiosk.presentation.signalR.listeners.SignalRConnectionListener
import net.invictusmanagement.invictuskiosk.presentation.signalR.listeners.MobileChatHubEventListener
import net.invictusmanagement.invictuskiosk.presentation.signalR.MobileChatHubManager
import net.invictusmanagement.invictuskiosk.util.ConnectionState
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import net.invictusmanagement.invictuskiosk.util.SignalRConnectionState
import javax.inject.Inject

@HiltViewModel
class VideoCallViewModel @Inject constructor(
    private val repository: VideoCallRepository,
    private val screenSaverRepository: ScreenSaverRepository,
    private val relayManagerRepository: RelayManagerRepository,
    private val logger: GlobalLogger
) : ViewModel(), MobileChatHubEventListener {

    private var room: Room? = null
    private var cameraCapturer: VideoCapturer? = null
    private val timeOutSeconds: Int = 45
    private val reconnectionTimeoutSeconds: Int = 15
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var videoTrack by mutableStateOf<LocalVideoTrack?>(null)
        private set
    var audioTrack by mutableStateOf<LocalAudioTrack?>(null)
        private set
    var remoteVideoTrack by mutableStateOf<RemoteVideoTrack?>(null)
        private set
    var connectionState by mutableStateOf(ConnectionState.CONNECTING)
        private set
    var signalRConnectionState by mutableStateOf(SignalRConnectionState.CONNECTING)
        private set
    var remainingSeconds by mutableIntStateOf(timeOutSeconds)
        private set
    var callEndedDueToMissedCall by mutableStateOf(false)
        private set
    var sendToVoiceMail by mutableStateOf(false)
        private set
    var isAccessGranted by mutableStateOf(false)
        private set
    var tokenFetchAttemptCount by mutableIntStateOf(0)
        private set
    var isFetchingToken by mutableStateOf(false)
        private set
    var showVoiceMailDialog by mutableStateOf(false)
        private set
    private var missedCallJob: Job? = null
    private var isMissedCallTimerPaused = false
    private var missedCallSecondsLeft = timeOutSeconds
    private var reconnectionJob: Job? = null
    private var disconnectTimerJob: Job? = null
    private var isTimerPaused = false


    private var remoteParticipantJoined = false

    private var mobileChatHubManager: MobileChatHubManager? = null
    private var chatHubManager: ChatHubManager? = null

    fun initializeMobileChatHub(kioskId: Int) {
        if (mobileChatHubManager != null) return

        signalRConnectionState = SignalRConnectionState.CONNECTING

        mobileChatHubManager = MobileChatHubManager(
            kioskId = kioskId,
            listener = this,
            connectionListener = object : SignalRConnectionListener {
                override fun onConnected() {
                    if (connectionState == ConnectionState.CONNECTING) {
                        signalRConnectionState = SignalRConnectionState.CONNECTED
                    }
                }
                override fun onConnectionError(method: String, e: Exception) {
                    logger.logError("SignalRConnectionError/VideoCallViewModel/${method}", "Error connecting to SignalR: ${e.localizedMessage}", e)
                }
            }
        )

        mobileChatHubManager?.connect()
    }

    fun initializeChatHub(id: Int) {
        if (chatHubManager != null) return

        chatHubManager = ChatHubManager(
            groupName = id.toString(),
            networkMonitor =  networkMonitor,
        )

        viewModelScope.launch {
            chatHubManager?.connect()
        }
    }

    fun connectToVideoCallWithRetry(
        context: Context,
        kioskActivationCode: String,
        kioskName: String?,
        residentActivationCode: String,
        maxRetries: Int = 5,
        delayBetweenRetriesMillis: Long = 2000,
        onAllRetriesFailed: () -> Unit = {}
    ) {
        viewModelScope.launch {
            connectionState = ConnectionState.CONNECTING
            tokenFetchAttemptCount = 0
            var success = false

            repeat(maxRetries) { attempt ->
                tokenFetchAttemptCount = attempt + 1

                try {
                    // Get token
                    var newToken: VideoCallToken? = null
                    repository.getVideoCallToken(kioskActivationCode).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                val token = result.data?.token
                                if (!token.isNullOrEmpty()) {
                                    newToken = VideoCallToken(token = token)
                                }
                            }

                            is Resource.Error -> {
                                Log.e("VideoCall", "Token fetch failed: ${result.message}")
                            }

                            is Resource.Loading -> {
                                // Handle loading state if needed
                            }
                        }
                    }

                    if (newToken == null) {
                        Log.d("VideoCall", "Token fetch failed — retrying...")
                        delay(delayBetweenRetriesMillis)
                        return@repeat
                    }

                    val connected = CompletableDeferred<Boolean>()

                    // Try connecting to room
                    connectToRoom(
                        context = context,
                        accessToken = newToken.token,
                        roomName = kioskActivationCode,
                        onConnected = {
                            connected.complete(true)
                        },
                        onDisconnected = {
                            if (!connected.isCompleted) connected.complete(false)
                        },
                        onMissedCall = {
                            postMissedCall(kioskName ?: "", residentActivationCode)
                            showVoiceMailDialog = true
                        }
                    )

                    val result = withTimeoutOrNull(10_000) {
                        connected.await()
                    }

                    // If connectToRoom succeeded (Twilio connected)
                    if (result == true) {
                        success = true
                        Log.d(
                            "VideoCall",
                            "Video call setup successful on attempt $tokenFetchAttemptCount"
                        )
                        return@launch // stop retry loop, connection established
                    } else {
                        Log.w("VideoCall", "Connection attempt timed out or failed — retrying...")
                    }

                } catch (e: Exception) {
                    logger.logError("connectToVideoCall", "Error connecting to video call ${e.localizedMessage}", e)
                    Log.e(
                        "VideoCall",
                        "Attempt $tokenFetchAttemptCount failed with exception: ${e.message}"
                    )
                }

                // Wait before next retry
                delay(delayBetweenRetriesMillis)
            }

            // All retries failed
            if (!success) {
                connectionState = ConnectionState.FAILED
                onAllRetriesFailed()
                Log.e("VideoCall", "Failed to connect after $maxRetries attempts.")
            }
        }
    }


    private fun initializeTracks(context: Context) {
            cameraCapturer = Camera2Capturer(
                context,
                getAvailableCameraId(context),
                object : Camera2Capturer.Listener {
                    override fun onFirstFrameAvailable() {}
                    override fun onCameraSwitched(newCameraId: String) {}
                    override fun onError(error: Camera2Capturer.Exception) {
                        Log.e("CameraCapturer", "Camera error: ${error.message}")
                    }
                }
            )

            videoTrack = LocalVideoTrack.create(context, true, cameraCapturer!!)
            audioTrack = LocalAudioTrack.create(context, true)
    }

    fun connectToRoom(
        context: Context,
        accessToken: String,
        roomName: String,
        onConnected: () -> Unit,
        onDisconnected: () -> Unit,
        onMissedCall: () -> Unit
    ) {
        try {
            initializeTracks(context)
        }catch (e: Exception) {
            logger.logError("initializeTracks", "Failed to initialize tracks ${e.localizedMessage}", e)
            errorMessage = Constants.getFriendlyCameraError(e)
            viewModelScope.launch {
                delay(2000)
                disconnect()
            }
        }
        pauseScreenSaver()

        val connectOptions = ConnectOptions.Builder(accessToken)
            .roomName(roomName)
            .audioTracks(listOfNotNull(audioTrack))
            .videoTracks(listOfNotNull(videoTrack))
            .region("gll")          // best Twilio edge automatically
            .build()

        room = Video.connect(context, connectOptions, object : Room.Listener {
            override fun onConnected(room: Room) {
                connectionState = ConnectionState.CONNECTED
                room.remoteParticipants.forEach { participant ->
                    handleRemoteParticipant(participant)
                }
                onConnected()

                // Start 45s timeout for missed call detection
                startMissedCallTimeout(onMissedCall)
                // Start 45-second disconnect timer
                startDisconnectTimerWithCountdown()

            }

            override fun onParticipantConnected(room: Room, participant: RemoteParticipant) {
                pauseScreenSaver()
                handleRemoteParticipant(participant)
            }

            override fun onDisconnected(room: Room, e: TwilioException?) {
                connectionState = ConnectionState.DISCONNECTED
                resumeScreenSaver()
                if (!callEndedDueToMissedCall && !sendToVoiceMail) {
                    onDisconnected()
                }
                callEndedDueToMissedCall = false // Reset here AFTER decision
            }

            override fun onConnectFailure(room: Room, e: TwilioException) {
                logger.logError("twilio/onConnectFailure", "Failed to connect to Twilio room ${e.localizedMessage}", e)
                connectionState = ConnectionState.FAILED
                resumeScreenSaver()
                onDisconnected()
            }

            override fun onParticipantDisconnected(room: Room, participant: RemoteParticipant) {
                resumeScreenSaver()
                if (sendToVoiceMail) {
                    disconnect()
                }
            }

            override fun onReconnecting(room: Room, e: TwilioException) {
                connectionState = ConnectionState.RECONNECTING
                pauseScreenSaver()
                pauseMissedCallTimer()
                pauseDisconnectTimer()
                startReconnectionWatchdog()  // start reconnect timeout counter
            }

            override fun onReconnected(room: Room) {
                connectionState = ConnectionState.RECONNECTED
                cancelReconnectionWatchdog()  // Stop reconnect timeout

                room.remoteParticipants.forEach { participant ->
                    handleRemoteParticipant(participant)
                }

                resumeMissedCallTimer(onMissedCall)
                resumeDisconnectTimer()
            }

            override fun onRecordingStarted(room: Room) {}
            override fun onRecordingStopped(room: Room) {}
        })

    }

    private fun handleRemoteParticipant(participant: RemoteParticipant) {
        participant.setListener(object : RemoteParticipant.Listener {
            override fun onAudioTrackPublished(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication
            ) {
            }

            override fun onAudioTrackUnpublished(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication
            ) {

            }

            override fun onAudioTrackSubscribed(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
                remoteAudioTrack: RemoteAudioTrack
            ) {

            }

            override fun onAudioTrackSubscriptionFailed(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
                twilioException: TwilioException
            ) {

            }

            override fun onAudioTrackUnsubscribed(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
                remoteAudioTrack: RemoteAudioTrack
            ) {

            }

            override fun onVideoTrackPublished(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication
            ) {

            }

            override fun onVideoTrackUnpublished(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication
            ) {

            }

            override fun onVideoTrackSubscribed(
                participant: RemoteParticipant,
                publication: RemoteVideoTrackPublication,
                videoTrack: RemoteVideoTrack
            ) {
                remoteParticipantJoined = true
                missedCallJob?.cancel() // Cancel missed call timer
                missedCallJob = null
                remoteVideoTrack = videoTrack
            }

            override fun onVideoTrackUnsubscribed(
                participant: RemoteParticipant,
                publication: RemoteVideoTrackPublication,
                videoTrack: RemoteVideoTrack
            ) {
                remoteVideoTrack = null
                disconnect()
            }

            override fun onDataTrackPublished(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication
            ) {
            }

            override fun onDataTrackUnpublished(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication
            ) {

            }

            override fun onDataTrackSubscribed(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
                remoteDataTrack: RemoteDataTrack
            ) {

            }

            override fun onDataTrackSubscriptionFailed(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
                twilioException: TwilioException
            ) {

            }

            override fun onDataTrackUnsubscribed(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
                remoteDataTrack: RemoteDataTrack
            ) {

            }

            override fun onAudioTrackEnabled(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication
            ) {

            }

            override fun onAudioTrackDisabled(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication
            ) {

            }

            override fun onVideoTrackEnabled(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication
            ) {

            }

            override fun onVideoTrackDisabled(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication
            ) {

            }

            override fun onVideoTrackSubscriptionFailed(
                participant: RemoteParticipant,
                publication: RemoteVideoTrackPublication,
                exception: TwilioException
            ) {
            }
        })
    }

    private fun getAvailableCameraId(context: Context): String {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        var backCameraId: String? = null

        cameraManager.cameraIdList.forEach { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)

            when (lensFacing) {
                CameraCharacteristics.LENS_FACING_FRONT -> {
                    return id
                }

                CameraCharacteristics.LENS_FACING_BACK -> {
                    backCameraId = id
                }
            }
        }

        backCameraId?.let {
            Log.w("CameraCheck", "No front camera found. Falling back to back camera: $it")
            return it
        }
        throw IllegalStateException("No available cameras found on this device.")
    }


    fun connectToVideoCall(accessPointId: Int, residentActivationCode: String) {
        repository.connectToVideoCall(
            VideoCallDto(
                accessPointId = accessPointId,
                residentActivationCode = residentActivationCode
            )
        ).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    Log.d("VideoCall", "connectToVideoCall: Video call connected")
                }

                is Resource.Error -> {
                    Log.d("TAG", "connectToVideoCall: ${result.message}")
                }

                is Resource.Loading -> {}
            }
        }.launchIn(viewModelScope)
    }

    fun postMissedCall(kioskName: String, residentActivationCode: String) {
        repository.postMissedCall(
            MissedCallDto(
                kioskName = kioskName,
                residentActivationCode = residentActivationCode
            )
        ).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    Log.d("TAG", "postMissedCall: Missed call posted")
                }

                is Resource.Error -> {
                    Log.d("TAG", "postMissedCall: ${result.message}")
                }

                is Resource.Loading -> {}
            }
        }.launchIn(viewModelScope)
    }

    private fun startDisconnectTimerWithCountdown() {
        disconnectTimerJob?.cancel()
        isTimerPaused = false

        disconnectTimerJob = viewModelScope.launch {
            var secondsLeft = remainingSeconds

            while (secondsLeft > 0 && !isTimerPaused) {
                delay(1000)
                secondsLeft--
                remainingSeconds = secondsLeft
            }

            if (!isTimerPaused && secondsLeft == 0) {
                disconnect()
            }
        }
    }

    private fun startMissedCallTimeout(
        onMissedCall: () -> Unit,
        initialSeconds: Int = timeOutSeconds
    ) {
        missedCallJob?.cancel()
        isMissedCallTimerPaused = false
        missedCallSecondsLeft = initialSeconds

        missedCallJob = viewModelScope.launch {
            while (missedCallSecondsLeft > 0 && !isMissedCallTimerPaused) {
                delay(1000)
                missedCallSecondsLeft--
            }

            if (!isMissedCallTimerPaused && missedCallSecondsLeft == 0) {
                if (!remoteParticipantJoined) {
                    callEndedDueToMissedCall = true
                    disconnect()
                    onMissedCall()
                }
            }
        }
    }

    private fun startReconnectionWatchdog() {
        reconnectionJob?.cancel()
        reconnectionJob = CoroutineScope(Dispatchers.IO).launch {
            delay((reconnectionTimeoutSeconds * 1000).toLong())
            if (connectionState == ConnectionState.RECONNECTING) {
                // Twilio never recovered — treat as disconnect
                disconnect()
            }
        }
    }

    private fun cancelReconnectionWatchdog() {
        reconnectionJob?.cancel()
        reconnectionJob = null
    }

    private fun pauseDisconnectTimer() {
        isTimerPaused = true
        disconnectTimerJob?.cancel()
    }

    private fun resumeDisconnectTimer() {
        if (remainingSeconds <= 0) return

        isTimerPaused = false
        startDisconnectTimerWithCountdown()
    }

    private fun pauseMissedCallTimer() {
        isMissedCallTimerPaused = true
        missedCallJob?.cancel()
    }

    private fun resumeMissedCallTimer(onMissedCall: () -> Unit) {
        if (missedCallSecondsLeft <= 0) return

        isMissedCallTimerPaused = false

        startMissedCallTimeout(
            onMissedCall = onMissedCall,
            initialSeconds = missedCallSecondsLeft
        )
    }


    fun disconnect() {
        try {
            chatHubManager?.endVideoCallInMobile()

            // Cancel disconnect countdown timer
            disconnectTimerJob?.cancel()
            disconnectTimerJob = null
            isTimerPaused = false

            // Cancel reconnection watchdog
            reconnectionJob?.cancel()
            reconnectionJob = null

            // Cancel missed call timer
            missedCallJob?.cancel()
            missedCallJob = null

            remoteParticipantJoined = false

            // Cancel ongoing token fetch or retries
            viewModelScope.coroutineContext.cancelChildren()

            room?.disconnect()
            room = null

            videoTrack?.release()
            videoTrack = null

            audioTrack?.release()
            audioTrack = null

            // Clean up SignalR connections
            mobileChatHubManager?.cleanup()
            chatHubManager?.cleanup()

            connectionState = ConnectionState.DISCONNECTED

        } catch (e: Exception) {
            Log.e("TAG", "disconnect: ${e.message}")
            connectionState = ConnectionState.DISCONNECTED
        } finally {
            resumeScreenSaver()
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

    fun pauseScreenSaver() {
        screenSaverRepository.pauseScreenSaver()
    }

    fun resumeScreenSaver() {
        screenSaverRepository.resumeScreenSaver()
    }

    override fun onSendToVoiceMail() {
        connectionState = ConnectionState.DISCONNECTED
        sendToVoiceMail = true
        disconnect()
    }

    override fun onOpenAccessPoint(
        relayPort: Int,
        relayOpenTimer: Int,
        relayDelayTimer: Int,
        silent: Boolean
    ) {
        viewModelScope.launch {
            //send open AccessPoint request to the relay manager when get the access granted via video call
            relayManagerRepository.openAccessPoint(
                relayPort,
                relayOpenTimer,
                relayDelayTimer
            )
        }
        isAccessGranted = true
        disconnect()

    }

    fun setVoiceMailDialogVisibility(isVisible: Boolean) {
        showVoiceMailDialog = isVisible
    }
}