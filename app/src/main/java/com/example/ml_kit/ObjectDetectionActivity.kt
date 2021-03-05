package com.example.ml_kit

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.ml_kit.utils.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.objects.defaults.PredefinedCategory
import kotlinx.android.synthetic.main.activity_object_detection.*
import java.lang.StringBuilder

class ObjectDetectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_object_detection)
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
                textOutput.text = "Your request is in progress,Please wait.."
                textOutput.text = getObject(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
            }
        }

    }

    private suspend fun getObject(image: Image, rotationDegrees: Int): String {
        val inputImage = InputImage.fromMediaImage(image, rotationDegrees)
        imageView.setImageBitmap(inputImage.bitmapInternal)

        val options = ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableClassification()
                .enableMultipleObjects()
                .build()

        val objectDetector = ObjectDetection.getClient(options)

        var detectedObjects: List<DetectedObject>? = null
        val outputText = StringBuilder()
        objectDetector.process(inputImage)
                .addOnSuccessListener { results ->
                    detectedObjects = results
                    Log.d("MLKit", " objects= ${results.toString()}")
                    outputText.append("Success:-")
                }
                .addOnFailureListener { e ->
                    outputText.append(e.toString())
                }.await().toString()

        detectedObjects?.let {
            for (detectedObject in it) {
                val boundingBox = detectedObject.boundingBox
                val trackingId = detectedObject.trackingId
                Log.d("MLKit", " label= ${detectedObject.labels.toString()}")

                for (label in detectedObject.labels) {
                    outputText.append(label.text+", ")

                      /*if (PredefinedCategory.FOOD == labelText) {

                      }
                      val index = label.index
                      if (PredefinedCategory.FOOD_INDEX == index) {

                      }
                     text= label.confidence.toString()*/
                }
            }
        }?:outputText.append("detectedObjects are empty")
        Log.d("MLKit", " objects= ${outputText.toString()}")
        return outputText.deleteCharAt(outputText.length - 1).toString()
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