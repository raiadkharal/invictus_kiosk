package net.invictusmanagement.invictuskiosk.presentation.voice_mail

import android.Manifest
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.domain.repository.ScreenSaverRepository
import net.invictusmanagement.invictuskiosk.domain.repository.VoicemailRepository
import java.io.File
import javax.inject.Inject
import kotlin.math.truncate

@HiltViewModel
class VoicemailViewModel @Inject constructor(
    private val repository: VoicemailRepository,
    private val screenSaverRepository: ScreenSaverRepository
) : ViewModel() {

    private var onFinishCallback: ((File) -> Unit)? = null
    private var videoFile: File? = null

    private val _countdown = mutableIntStateOf(6)
    val countdown: State<Int> = _countdown

    private val _isRecordingStarted = mutableStateOf(false)
    val isRecordingStarted: State<Boolean> = _isRecordingStarted

    private val _uploadState = MutableStateFlow(UploadState())
    val uploadState: StateFlow<UploadState> = _uploadState

    private val _videoCapture = mutableStateOf<VideoCapture<Recorder>?>(null)
    val videoCapture: State<VideoCapture<Recorder>?> = _videoCapture

    private var recording: Recording? = null

    fun startCountdown() {
        viewModelScope.launch {
            while (_countdown.intValue > 0) {
                delay(1000)
                _countdown.intValue -= 1
            }
            pauseScreenSaver()
            _isRecordingStarted.value = true
        }
    }

    fun uploadVoicemail(file: File, userId: Long) {
        repository.uploadVoicemail(file, userId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    resumeScreenSaver()
                    _uploadState.value = UploadState(data = result.data ?: 1)
                }

                is Resource.Error -> {
                    resumeScreenSaver()
                    _uploadState.value =
                        UploadState(error = result.message ?: "An unexpected error occurred")
                }

                is Resource.Loading -> {
                    _uploadState.value = UploadState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun setupCamera(
        previewView: PreviewView,
        context: Context,
        lifecycleOwner: LifecycleOwner
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                surfaceProvider = previewView.surfaceProvider
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()

            val videoCap = VideoCapture.withOutput(recorder)
            _videoCapture.value = videoCap

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                videoCap
            )
        }, ContextCompat.getMainExecutor(context))
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording(
        context: Context,
        onFinish: (File) -> Unit
    ) {
        val capture = videoCapture.value ?: return

        videoFile = File(context.cacheDir, "voicemail_${System.currentTimeMillis()}.mp4")
        val outputOptions = FileOutputOptions.Builder(videoFile!!).build()
        onFinishCallback = onFinish

        recording = capture.output
            .prepareRecording(context, outputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { event ->
                if (event is VideoRecordEvent.Finalize) {
                    if (!event.hasError()) {
                        onFinishCallback?.invoke(videoFile!!)
                    } else {
                        Log.e("Voicemail", "Recording error: ${event.error}")
                    }
                }
            }

        Handler(Looper.getMainLooper()).postDelayed({
            recording?.stop()
        }, 30_000)
    }

    fun stopRecording() {
        recording?.stop()
        recording = null

        resumeScreenSaver()
    }

    fun pauseScreenSaver() {
        screenSaverRepository.pauseScreenSaver()
    }

    fun resumeScreenSaver() {
        screenSaverRepository.resumeScreenSaver()
    }

    override fun onCleared() {
        super.onCleared()
        stopRecording()
    }

    fun resetState() {
        _countdown.intValue = 12
        _isRecordingStarted.value = false
    }
}
