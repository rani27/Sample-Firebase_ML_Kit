package com.example.ml_kit.utils

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.ComponentActivity
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.io.IOException
import java.io.InputStream


fun ComponentActivity.launchWhenResumed(block: suspend CoroutineScope.() -> Unit): Job =
    lifecycle.coroutineScope.launchWhenResumed(block)

fun String.getBitmapFromAsset(context: Context): Bitmap? {
    val assetManager: AssetManager = context.getAssets()
    val istr: InputStream
    var bitmap: Bitmap? = null
    try {
        istr = assetManager.open(this!!)
        bitmap = BitmapFactory.decodeStream(istr)
    } catch (e: IOException) {
        return null
    }
    return bitmap
}