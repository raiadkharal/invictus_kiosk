package net.invictusmanagement.invictuskiosk.presentation.video_call

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import android.widget.Toast
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.dto.MissedCallDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.VideoCallDto
import net.invictusmanagement.invictuskiosk.domain.model.VideoCallToken
import net.invictusmanagement.invictuskiosk.domain.repository.VideoCallRepository
import net.invictusmanagement.invictuskiosk.domain.repository.ScreenSaverRepository
import net.invictusmanagement.invictuskiosk.presentation.signalR.SignalREventListener
import net.invictusmanagement.invictuskiosk.presentation.signalR.SignalRManager
import net.invictusmanagement.invictuskiosk.util.ConnectionState
import javax.inject.Inject

@HiltViewModel
class VideoCallViewModel @Inject constructor(
    private val repository: VideoCallRepository,
    private val screenSaverRepository: ScreenSaverRepository
) : ViewModel(), SignalREventListener {

    private var room: Room? = null
    private var cameraCapturer: VideoCapturer? = null
    private val timeOutSeconds: Int = 45

    var token by mutableStateOf(VideoCallToken(token = ""))
        private set

    var videoTrack by mutableStateOf<LocalVideoTrack?>(null)
        private set

    var audioTrack by mutableStateOf<LocalAudioTrack?>(null)
        private set

    var remoteVideoTrack by mutableStateOf<RemoteVideoTrack?>(null)
        private set

    var connectionState by mutableStateOf(ConnectionState.CONNECTING)
        private set

    var remainingSeconds by mutableIntStateOf(timeOutSeconds)
        private set

    var sendToVoiceMail by mutableStateOf(false)
        private set

    private var missedCallJob: Job? = null
    private var remoteParticipantJoined = false
    private var callEndedDueToMissedCall = false


    private var signalRManager: SignalRManager? = null

    fun initializeSignalR(kioskId: Int) {
        signalRManager = SignalRManager(kioskId, this)
        signalRManager?.connect()
    }

    fun getVideoCallToken(room: String) {
        repository.getVideoCallToken(room).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    token = result.data ?: VideoCallToken(token = "")
                }

                is Resource.Error -> {

                }

                is Resource.Loading -> {

                }
            }
        }.launchIn(viewModelScope)
    }

    private fun initializeTracks(context: Context) {
        try {
            cameraCapturer = Camera2Capturer(
                context,
                getAvailableFrontCameraId(context),
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
        }catch (ex: Exception){
            Toast.makeText(context, ex.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    fun connectToRoom(
        context: Context,
        accessToken: String,
        roomName: String,
        onConnected: () -> Unit,
        onDisconnected: () -> Unit,
        onMissedCall: () -> Unit
    ) {
        initializeTracks(context)
        pauseScreenSaver()

        val connectOptions = ConnectOptions.Builder(accessToken)
            .roomName(roomName)
            .audioTracks(listOfNotNull(audioTrack))
            .videoTracks(listOfNotNull(videoTrack))
            .build()

        room = Video.connect(context, connectOptions, object : Room.Listener {
            override fun onConnected(room: Room) {
                connectionState = ConnectionState.CONNECTED
                room.remoteParticipants.forEach { participant ->
                    handleRemoteParticipant(participant)
                }
                onConnected()

                // Start 45-second disconnect timer
                startDisconnectTimerWithCountdown()
                // Start 45s timeout for missed call detection
                startMissedCallTimeout(onMissedCall)

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
                connectionState = ConnectionState.FAILED
                resumeScreenSaver()
            }

            override fun onParticipantDisconnected(room: Room, participant: RemoteParticipant) {
                resumeScreenSaver()
                if(sendToVoiceMail) {
                    disconnect()
                }
            }

            override fun onReconnecting(room: Room, e: TwilioException) {}
            override fun onReconnected(room: Room) {}
            override fun onRecordingStarted(room: Room) {}
            override fun onRecordingStopped(room: Room) {}
        })

    }

    private fun startDisconnectTimerWithCountdown() {
        viewModelScope.launch {
            for (i in timeOutSeconds-1 downTo 0) {
                delay(1000)
                remainingSeconds = i
            }
            disconnect()
        }
    }

    private fun startMissedCallTimeout(onMissedCall: () -> Unit) {
        missedCallJob?.cancel()
        missedCallJob = viewModelScope.launch {
            delay((timeOutSeconds*1000).toLong()) // 45 seconds
            if (!remoteParticipantJoined) {
                callEndedDueToMissedCall = true
                disconnect()
                onMissedCall()
            }
        }
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

    fun disconnect() {
        missedCallJob?.cancel()
        missedCallJob = null
        remoteParticipantJoined = false

        room?.disconnect()
        room = null
        videoTrack?.release()
        audioTrack?.release()
        videoTrack = null
        audioTrack = null
        connectionState = ConnectionState.DISCONNECTED
        resumeScreenSaver()
    }

    private fun getAvailableFrontCameraId(context: Context): String {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager.cameraIdList.forEach { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (cameraDirection == CameraCharacteristics.LENS_FACING_FRONT) {
                return id
            }
        }
        throw IllegalStateException("No front-facing camera found.")
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
        ).onEach { result->
            when(result){
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

}