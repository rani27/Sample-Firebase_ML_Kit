package com.example.ml_kit

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.ml_kit.utils.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.android.synthetic.main.activity_text_recognition.*

class TextRecognitionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_recognition)
        cameraPermissions()
    }

    private fun cameraPermissions() {
        if (hasPermission(Manifest.permission.CAMERA)
        ) {
            launchWhenResumed {
                bindUseCases(getCameraProvider())
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION)
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun bindUseCases(cameraProvider: ProcessCameraProvider) {
        val preview = buildPreview()
        val takePicture = buildTakePicture()
        val cameraSelector = buildCameraSelector()

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, takePicture)

        button.setOnClickListener {
            launchWhenResumed {
                val imageProxy = takePicture.takePicture(executor)
                textOutput.text="Your request is in progress,Please wait.."
                val text= getText(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
                textOutput.text=text
            }
        }

    }
    private suspend fun getText(image: Image, rotationDegrees: Int): String {
        val imageInput = InputImage.fromMediaImage(image, rotationDegrees)
        return TextRecognition.getClient().process(imageInput).await().text
    }
    private fun buildPreview(): Preview = Preview.Builder()
        .build()
        .apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

    private fun buildCameraSelector(): CameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    private fun buildTakePicture(): ImageCapture = ImageCapture.Builder()
        .setTargetRotation(previewView.display.rotation)
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchWhenResumed {
                bindUseCases(getCameraProvider())
            }
        }
    }
}