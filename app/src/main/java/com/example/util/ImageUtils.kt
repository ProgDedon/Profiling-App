package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    suspend fun saveImageLocally(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val photosDir = File(context.filesDir, "photos")
            if (!photosDir.exists()) photosDir.mkdirs()

            val fileName = "profile_${System.currentTimeMillis()}.jpg"
            val targetFile = File(photosDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                // Decode scaled down if it's huge, to save space and fast render
                val original = BitmapFactory.decodeStream(input) ?: return@withContext null
                val maxDim = 1200
                val ratio = (original.width.toFloat() / original.height.toFloat())
                val newWidth = if (original.width > original.height) {
                    original.width.coerceAtMost(maxDim)
                } else {
                    (original.height.coerceAtMost(maxDim) * ratio).toInt()
                }
                val newHeight = (newWidth / ratio).toInt()

                val scaled = if (newWidth < original.width || newHeight < original.height) {
                    Bitmap.createScaledBitmap(original, newWidth, newHeight, true)
                } else {
                    original
                }

                FileOutputStream(targetFile).use { out ->
                    scaled.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
            }
            targetFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }
}
