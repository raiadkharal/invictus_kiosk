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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class SnapshotManager(
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
    suspend fun startCamera(previewProvider: () -> androidx.camera.view.PreviewView) = withContext(Dispatchers.Main) {
        val previewView = previewProvider()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().apply {
            surfaceProvider = previewView.surfaceProvider
        }


        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        // video recorder & capture
        val recorder = Recorder.Builder()
            .setExecutor(ContextCompat.getMainExecutor(context))
            .setQualitySelector(QualitySelector.from(Quality.SD)) // choose quality
            .build()

        videoCapture = VideoCapture.withOutput(recorder)

        val frontCameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        val backCameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val cameraSelector = if (cameraProvider.hasCamera(frontCameraSelector)) {
            frontCameraSelector
        } else {
            backCameraSelector
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                (context as androidx.lifecycle.LifecycleOwner),
                cameraSelector,
                preview,
                imageCapture,
                videoCapture
            )
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to bind camera use cases", ex)
            lastError = "Failed to bind camera: ${ex.localizedMessage}"
        }
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
    fun stopStampRecordingAndSend(isValid: Boolean? = null) {
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
        val tmp = createTempFile("stamp_img_", ".jpg", context.cacheDir)
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
            // create multipart body
            val videoReqBody = formFile.asRequestBody("video/mp4".toMediaTypeOrNull())
            val videoPart = MultipartBody.Part.createFormData("videoFile", formFile.name, videoReqBody)

            val userIdPart = MultipartBody.Part.createFormData("userId", residentUserId.toString())
            val imagePart = MultipartBody.Part.createFormData("image", stampImageBase64 ?: "")

            // optional fields - use placeholders or adapt to real values
            val recipientPart = MultipartBody.Part.createFormData("recipient", "recipient-placeholder")
            val accessLogIdPart = MultipartBody.Part.createFormData("accessLogId", "0")
            val serviceKeyUsagePart = MultipartBody.Part.createFormData("serviceKeyUsageId", "0")

            val isValidPart = MultipartBody.Part.createFormData("isValid", "false")

            // call API
            val response = api.saveStampVideo(
                videoPart,
                userIdPart, imagePart,
                recipientPart, accessLogIdPart, serviceKeyUsagePart, isValidPart
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
    val imageBase64: String
)
