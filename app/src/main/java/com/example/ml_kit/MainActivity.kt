package com.example.ml_kit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scanning.setOnClickListener{
            startActivity(Intent(this,CardScanningActivity::class.java))
        }
        textRecognition.setOnClickListener{
            startActivity(Intent(this, TextRecognitionActivity::class.java))
        }
        textObject.setOnClickListener{
            startActivity(Intent(this,ObjectDetectionActivity::class.java))
        }
        textBarcode.setOnClickListener{
            startActivity(Intent(this, BarCodeScanningActivity::class.java))
        }
    }
}