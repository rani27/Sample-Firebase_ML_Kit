package com.example.ml_kit

import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.Image
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.ml_kit.utils.executor
import com.example.ml_kit.utils.getCameraProvider
import com.example.ml_kit.utils.*
import com.example.ml_kit.utils.hasPermission
import com.example.ml_kit.utils.launchWhenResumed
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.android.synthetic.main.activity_barcode.*

@SuppressLint("UnsafeExperimentalUsageError")
class BarCodeScanningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode)
        cameraPermissions()
    }

    private fun cameraPermissions() {
        if (hasPermission(CAMERA)
        ) {
            launchWhenResumed {
                bindUseCases(getCameraProvider())
            }
        } else {
            requestPermissions(arrayOf(CAMERA), CAMERA_PERMISSION)
        }
    }

    private fun bindUseCases(cameraProvider: ProcessCameraProvider) {
        val preview = buildPreview()
        val takePicture = buildTakePicture()
        val cameraSelector = buildCameraSelector()

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, takePicture)

        button.setOnClickListener {
            launchWhenResumed {
                val imageProxy = takePicture.takePicture(executor)
                textOutput.text = "Your request is in progress,Please wait.."
                textOutput.text = getBarCode(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

            }
        }

    }

    private suspend fun getBarCode(image: Image, rotationDegrees: Int): String {

        /* Code 128 (FORMAT_CODE_128)
         Code 39 (FORMAT_CODE_39)
         Code 93 (FORMAT_CODE_93)
         Codabar (FORMAT_CODABAR)
         EAN-13 (FORMAT_EAN_13)
         EAN-8 (FORMAT_EAN_8)
         ITF (FORMAT_ITF)
         UPC-A (FORMAT_UPC_A)
         UPC-E (FORMAT_UPC_E)
         QR Code (FORMAT_QR_CODE)
         PDF417 (FORMAT_PDF417)
         Aztec (FORMAT_AZTEC)
         Data Matrix (FORMAT_DATA_MATRIX)*/
        val inputImage = InputImage.fromMediaImage(image, rotationDegrees)
        /* val options = BarcodeScannerOptions.Builder()
             .setBarcodeFormats(
                 Barcode.FORMAT_QR_CODE,
                 Barcode.FORMAT_AZTEC)
             .build()*/

        val scanner = BarcodeScanning.getClient()
        val result = scanner.process(inputImage)
                .addOnSuccessListener {
                    textOutput.text = it.toString()

                }
                .addOnFailureListener {
                    textOutput.text = it.toString()

                }.await().toString()
        return result
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
        if (requestCode == CAMERA_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
            launchWhenResumed {
                bindUseCases(getCameraProvider())
            }
        }
    }
}
