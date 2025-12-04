package net.invictusmanagement.invictuskiosk.commons

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject

class SnapshotManager @Inject constructor (
    private val context: Context,
    private val api: ApiInterface
) {
    private val TAG = "SnapshotManager"

    // CameraX objects (to be created by startCamera)
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    // recording state
    private var activeRecording: Recording? = null
    private var currentVideoFile: File? = null
    private var stampImageBase64: String? = null
    private var residentUserId: Long = 0L
    private var accessLogId: Long = 0L
    private var serviceKeyUsageId: Long = 0L
    private var isValid: Boolean = false

    // simple busy flag
    @Volatile
    var isBusy: Boolean = false
        private set

    // errors and state can be observed by Compose / UI if needed
    var lastError by mutableStateOf<String?>(null)

    /**
     * Initialize CameraX preview + capture usecases.
     * previewViewProvider must supply your PreviewView (from Compose host).
     * This function must be called when you have a LifecycleOwner (e.g. in Activity/Fragment's onCreate/onResume)
     */
    suspend fun startCamera(
        previewView: PreviewView,
        context: Context,
        lifecycleOwner: LifecycleOwner
    ) = withContext(Dispatchers.Main) {
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

                videoCapture = videoCap
                imageCapture = imageCap

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
     * Take a snapshot (low quality jpeg), convert to base64 and upload to /api/images
     * Returns server id (or throws).
     */
    suspend fun takeSnapshotAndUpload(): Long = withContext(Dispatchers.IO) {
        if (isBusy) throw IllegalStateException("SnapshotManager busy")
        isBusy = true
        try {
            val ic = imageCapture ?: throw IllegalStateException("ImageCapture not initialized")
            // temp file
            val tmp = createTempFile("snap_", ".jpg", context.cacheDir)
            val outputOptions = ImageCapture.OutputFileOptions.Builder(tmp).build()

            // wrap callback in suspendCancellableCoroutine
            val result = suspendCancellableCoroutine<Long> { cont ->
                ic.takePicture(outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        try {
                            // read file into bytes and compress / reduce quality if needed
                            val bytes = tmp.readBytes()
                            // re-encode with lower quality to match JS .toDataURL('image/jpeg', 0.2) (approx)
                            val compressed = compressJpegToQuality(bytes, 20) // 20% quality
                            val b64 = Base64.encodeToString(compressed, Base64.NO_WRAP)
                            // build request payload
                            val takenUtc = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }.format(Date())
                            // using API: expect server returns numeric id
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val req = ImageUploadRequest(takenUtc, "data:image/jpeg;base64,$b64")
                                    val resp = api.uploadImage(req) // suspend call
                                    // signal result to caller
                                    cont.resumeWith(Result.success(resp.body() ?: -1L))
                                } catch (e: Exception) {
                                    Log.e(TAG, "Image upload failed", e)
                                    cont.resumeWith(Result.failure(e))
                                } finally {
                                    tmp.delete()
                                }
                            }
                        } catch (e: Exception) {
                            tmp.delete()
                            cont.resumeWith(Result.failure(e))
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        tmp.delete()
                        cont.resumeWith(Result.failure(exception))
                    }
                })
            }

            result
        } finally {
            isBusy = false
        }
    }

    // Helper: naive recompress (read bytes -> decode -> re-encode). Use BitmapFactory + compress
    private fun compressJpegToQuality(original: ByteArray, qualityPercent: Int): ByteArray {
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(original, 0, original.size)
        val baos = ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, qualityPercent, baos)
        bitmap.recycle()
        return baos.toByteArray()
    }

    /**
     * Start recording stamp video for given userId and duration (seconds).
     * - captures a snapshot from preview when recording starts (low quality base64)
     * - records up to durationSec (if durationSec <= 0, user must call stopStampRecordingAndSend)
     * - uploads result form-data to server
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun recordStampVideoAndUpload(userId: Long, durationSec: Int = 300) {
        if (isBusy) {
            lastError = "SnapshotManager busy"
            return
        }
        isBusy = true
        residentUserId = userId
        stampImageBase64 = null
        lastError = null

        val vc = videoCapture ?: run {
            lastError = "VideoCapture not initialized"
            isBusy = false
            return
        }

        // create file
        val file = File(context.cacheDir, "stamp_${System.currentTimeMillis()}.mp4")
        currentVideoFile = file

        val mediaStoreOutput = FileOutputOptions.Builder(file).build()
        val recording = vc.output
            .prepareRecording(context, mediaStoreOutput)
            .apply  {
                withAudioEnabled()
            }
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        Log.d(TAG, "Recording started")
                        // capture image from imageCapture quickly (best-effort)
                        capturePreviewForStampImage()
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            Log.d(TAG, "Recording finalized: ${file.absolutePath}")
                            // upload file
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    uploadStampVideo(formFile = file)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Upload error", e)
                                    lastError = e.localizedMessage
                                } finally {
                                    // cleanup
                                    file.delete()
                                    isBusy = false
                                }
                            }
                        } else {
                            Log.e(TAG, "Recording error: ${recordEvent.error}")
                            lastError = "Recording error: ${recordEvent.error}"
                            file.delete()
                            isBusy = false
                        }
                    }
                    else -> {
                        // ignore other events
                    }
                }
            }

        activeRecording = recording

        // if duration provided, schedule stop
        if (durationSec > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(durationSec * 1000L)
                try {
                    activeRecording?.stop()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to stop recording", e)
                }
            }
        }
    }

    /**
     * Stops current recording and sends the video + metadata to server.
     * If isValid is provided, include in form-data similar to JS.
     */
    fun stopStampRecordingAndSend(serviceKeyUsageId: Long?,isValid: Boolean? = null,accessLogId: Long?) {
        this.accessLogId = accessLogId ?: 0
        this.isValid = isValid ?: true
        this.serviceKeyUsageId = serviceKeyUsageId ?: 0
        try {
            val rec = activeRecording
            if (rec != null) {
                // stop triggers Finalize event -> upload happens in callback
                rec.stop()
                activeRecording = null
            } else {
                // nothing recording
                isBusy = false
            }
        } catch (e: Exception) {
            lastError = e.localizedMessage
            isBusy = false
        }
    }

    /**
     * Capture a preview image using ImageCapture quickly for the stampImage - saved to stampImageBase64
     */
    private fun capturePreviewForStampImage() {
        val ic = imageCapture ?: return
        val tmp = createTempFile("stamp_img_", ".jpg", context.getExternalFilesDir(null))
        val outputOptions = ImageCapture.OutputFileOptions.Builder(tmp).build()
        ic.takePicture(outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                try {
                    val bytes = tmp.readBytes()
                    val compressed = compressJpegToQuality(bytes, 20)
                    val b64 = Base64.encodeToString(compressed, Base64.NO_WRAP)
                    stampImageBase64 = "data:image/jpeg;base64,$b64"
                } catch (e: Exception) {
                    Log.e(TAG, "Stamp image capture failed", e)
                } finally {
                    tmp.delete()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Error capturing stamp image: ${exception.message}")
            }
        })
    }

    /**
     * Uploads the current video file + stampImageBase64 + metadata to API endpoint.
     * Matches JS formData fields: videoFile, userId, recipient, accessLogId, serviceKeyUsageId, image, isValid
     */
    private suspend fun uploadStampVideo(formFile: File) = withContext(Dispatchers.IO) {
        try {
            Log.d("StampVideo", "Video file path = ${formFile.absolutePath}")
            Log.d("StampVideo", "Video file size = ${formFile.length()} bytes")
            // create multipart body
            val videoReqBody = formFile.asRequestBody("video/mp4".toMediaTypeOrNull())
            val videoPart = MultipartBody.Part.createFormData("VideoFile", formFile.name, videoReqBody)

            // Text form fields must use RequestBody!
            fun String.toRequestBody() =
                this.toRequestBody("text/plain".toMediaTypeOrNull())

            val userIdBody = residentUserId.toString().toRequestBody()
            val imageBody = (stampImageBase64 ?: "").toRequestBody()
            val recipientBody = "recipient-placeholder".toRequestBody()
            val accessLogIdBody = accessLogId.toString().toRequestBody()
            val serviceKeyUsageBody = serviceKeyUsageId.toString().toRequestBody()
            val isValidBody = isValid.toString().toRequestBody()

            // call API
            val response = api.saveStampVideo(
                videoPart,
                userIdBody,
                imageBody,
                recipientBody,
                accessLogIdBody,
                serviceKeyUsageBody,
                isValidBody
            )

            // handle response if needed
            Log.d(TAG, "stamp video uploaded, server returned: $response")
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP upload failed", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed", e)
            throw e
        } finally {
            // reset state
            residentUserId = 0
            stampImageBase64 = null
            currentVideoFile?.delete()
            currentVideoFile = null
        }
    }

    fun release() {
        try {
            cameraExecutor.shutdown()
        } catch (_: Exception) { }
    }
}


data class ImageUploadRequest(
    val takenUtc: String,
    val base64ImageBytes: String
)
