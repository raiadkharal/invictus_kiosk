package net.invictusmanagement.invictuskiosk.presentation.service_key

import android.Manifest
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
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
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.dto.ServiceKeyDto
import net.invictusmanagement.invictuskiosk.domain.model.AccessPoint
import net.invictusmanagement.invictuskiosk.domain.repository.RelayManagerRepository
import net.invictusmanagement.invictuskiosk.domain.repository.ServiceKeyRepository
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ServiceKeyViewModel @Inject constructor(
    private val repository: ServiceKeyRepository,
    private val dataStoreManager: DataStoreManager,
    private val relayRepository: RelayManagerRepository
):ViewModel() {
    private val _serviceKeyState = MutableStateFlow(ServiceKeyState())
    val serviceKeyState: StateFlow<ServiceKeyState> = _serviceKeyState

    private val _accessPoint = MutableStateFlow<AccessPoint?>(null)
    val accessPoint: StateFlow<AccessPoint?> = _accessPoint

    private val _videoCapture = mutableStateOf<VideoCapture<Recorder>?>(null)
    val videoCapture: State<VideoCapture<Recorder>?> = _videoCapture
    private var videoFile: File? = null

    private val _imageCapture = mutableStateOf<ImageCapture?>(null)
    val imageCapture: State<ImageCapture?> = _imageCapture

    private var recording: Recording? = null
    private var onFinishCallback: ((File) -> Unit)? = null

    init {
        viewModelScope.launch {
            dataStoreManager.accessPointFlow.collect {
                _accessPoint.value = it
            }
        }
    }

    fun validateServiceKey(serviceKeyDto: ServiceKeyDto){
        repository.validateServiceKey(serviceKeyDto).onEach { result->
            when(result){
                is Resource.Success ->{
                    if (result.data?.isValid == true) {
                        //send open AccessPoint request to the relay manager if the digital key is valid
                        relayRepository.openAccessPoint(
                            accessPoint.value?.relayPort,
                            accessPoint.value?.relayOpenTimer,
                            accessPoint.value?.relayDelayTimer
                        )
                    }

                    _serviceKeyState.value = ServiceKeyState(digitalKey = result.data)
                }
                is Resource.Error ->{
                    _serviceKeyState.value = ServiceKeyState(error = result.message ?: "An unexpected error occurred")
                }
                is Resource.Loading ->{
                    _serviceKeyState.value = ServiceKeyState(isLoading = true)
                }

            }
        }.launchIn(viewModelScope)
    }

    /**
     * Initialize the camera pipeline in the background.
     */
    fun initializeCamera(
        previewView: PreviewView,
        context: Context,
        lifecycleOwner: LifecycleOwner
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = ProcessCameraProvider.getInstance(context).get()

                val preview = Preview.Builder().build().apply {
                    surfaceProvider = previewView.surfaceProvider
                }

                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.HD))
                    .build()

                val videoCap = VideoCapture.withOutput(recorder)
                val imageCap = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                _videoCapture.value = videoCap
                _imageCapture.value = imageCap

                val frontCameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                val backCameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                val cameraSelector = if (cameraProvider.hasCamera(frontCameraSelector)) {
                    frontCameraSelector
                } else {
                    backCameraSelector
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCap,
                    imageCap
                )

                Log.d("ServiceKeyVM", "Camera initialized successfully.")
            } catch (e: Exception) {
                Log.e("ServiceKeyVM", "Camera initialization failed: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Start recording and capture a photo — both on background threads.
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording(context: Context, onFinish: ((File) -> Unit)? = null) {
        val capture = _videoCapture.value ?: return
        val photoCapture = _imageCapture.value

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val outputDir = File(context.getExternalFilesDir(null), "ServiceKeyRecordings")
                    .apply { if (!exists()) mkdirs() }

                val timestamp = System.currentTimeMillis()
                val videoFile = File(outputDir, "service_key_$timestamp.mp4")
                val photoFile = File(outputDir, "photo_$timestamp.jpg")

                // 📸 Capture still image (async)
                photoCapture?.let {
                    val photoOutput = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    it.takePicture(
                        photoOutput,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                Log.d("ServiceKeyVM", "Photo saved: ${photoFile.absolutePath}")
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("ServiceKeyVM", "Photo capture failed: ${exception.message}")
                            }
                        }
                    )
                }

                // 🎥 Start video recording (async)
                val outputOptions = FileOutputOptions.Builder(videoFile).build()
                recording = capture.output
                    .prepareRecording(context, outputOptions)
                    .withAudioEnabled()
                    .start(ContextCompat.getMainExecutor(context)) { event ->
                        if (event is VideoRecordEvent.Finalize) {
                            if (!event.hasError()) {
                                Log.d("ServiceKeyVM", "Video saved: ${videoFile.absolutePath}")
                                onFinish?.invoke(videoFile)
                            } else {
                                Log.e("ServiceKeyVM", "Recording error: ${event.error}")
                            }
                        }
                    }

                // 🕒 Auto-stop recording after 15 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    recording?.stop()
                    recording = null
                }, 15_000)
            } catch (e: Exception) {
                Log.e("ServiceKeyVM", "Error during recording: ${e.message}")
            }
        }
    }


    fun stopRecording() {
        recording?.stop()
        recording = null
    }

    override fun onCleared() {
        super.onCleared()
        recording?.stop()
        recording = null
    }
    fun resetServiceKeyState(){
        _serviceKeyState.value = ServiceKeyState()
    }
}