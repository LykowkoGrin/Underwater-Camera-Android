package com.example.underwaterphoto

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.PermissionChecker
import com.example.underwaterphoto.MainActivity.Companion
import com.example.underwaterphoto.databinding.ActivityMainBinding
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale


class EasyCamera(private val viewBinding: ActivityMainBinding,private val appActivity: AppCompatActivity) {

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private var camera: Camera? = null

    private lateinit var cameraExecutor: ExecutorService

    private var videoStatusListeners = mutableSetOf<(isVideoRecording: Boolean) -> Unit>()

    var isVideoRecording : Boolean = false
        private set

    fun addVideoStatusListener(videoStatusListener : (isVideoRecording: Boolean) -> Unit){
        videoStatusListeners.add( videoStatusListener )
    }
    fun removeVideoStatusListener(videoStatusListener : (isVideoRecording: Boolean) -> Unit){
        videoStatusListeners.remove( videoStatusListener )
    }

    init{
        videoStatusListeners.add { isVideoRecording ->
            this.isVideoRecording = isVideoRecording
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(appActivity)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                camera = cameraProvider
                    .bindToLifecycle(appActivity, cameraSelector, preview, imageCapture, videoCapture)
            } catch(exc: Exception) {
                Log.e(EasyCamera.TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(appActivity))
    }

    fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(appActivity.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(appActivity),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(EasyCamera.TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(appActivity.baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(EasyCamera.TAG, msg)
                }
            }
        )
    }

    fun startVideo() {
        val videoCapture = this.videoCapture ?: return

        recording?.let {
            it.stop()
            recording = null
        }

        val name = SimpleDateFormat(EasyCamera.FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(appActivity.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(appActivity, mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(
                        appActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(appActivity)) { recordEvent ->
                handleVideoEvent(recordEvent)
            }
    }


    fun stopVideo() {
        val curRecording = recording
        if (curRecording != null) {
            curRecording.stop()
            recording = null
        }
    }

    fun onDestroy() {
        cameraExecutor.shutdown()
    }

    private fun handleVideoEvent(recordEvent: VideoRecordEvent) {
        when (recordEvent) {
            is VideoRecordEvent.Start -> {
                videoStatusListeners.forEach { it(true) }
                Log.d(EasyCamera.TAG, "Video recording started.")
            }
            is VideoRecordEvent.Finalize -> {
                if (!recordEvent.hasError()) {
                    val msg = "Video capture succeeded: ${recordEvent.outputResults.outputUri}"
                    Toast.makeText(appActivity.baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(EasyCamera.TAG, msg)
                } else {
                    stopVideo()
                    Log.e(
                        EasyCamera.TAG,
                        "Video capture ends with error: ${recordEvent.error}"
                    )
                }
                videoStatusListeners.forEach { it(false) }
            }
        }
    }

    companion object {
        private const val TAG = "UnderwaterCamera"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}