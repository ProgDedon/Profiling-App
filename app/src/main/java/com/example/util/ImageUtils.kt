package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    suspend fun saveImageLocally(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        val photosDir = File(context.filesDir, "photos")
        if (!photosDir.exists()) photosDir.mkdirs()

        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val targetFile = File(photosDir, fileName)

        // 1. Copy stream to a temp file in cache first so we have a seekable, durable file
        val tempFile = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.tmp")
        try {
            val hasData = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output) > 0
                }
            } ?: false

            if (!hasData || !tempFile.exists() || tempFile.length() == 0L) {
                tempFile.delete()
                return@withContext null
            }

            // 2. Decode dimensions safely without loading entire image into memory
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(tempFile.absolutePath, boundsOptions)

            val origWidth = boundsOptions.outWidth
            val origHeight = boundsOptions.outHeight

            // If bounds are invalid, fallback to direct copy of raw bytes
            if (origWidth <= 0 || origHeight <= 0) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
                return@withContext targetFile.absolutePath
            }

            // 3. Compute inSampleSize to avoid any OutOfMemoryError
            val maxDimension = 1800
            var inSampleSize = 1
            while ((origWidth / inSampleSize) > maxDimension || (origHeight / inSampleSize) > maxDimension) {
                inSampleSize *= 2
            }

            val decodeOpts = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val decodedBitmap = BitmapFactory.decodeFile(tempFile.absolutePath, decodeOpts)
            if (decodedBitmap == null) {
                // Fallback to raw copy if decoding failed
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
                return@withContext targetFile.absolutePath
            }

            // 4. Handle EXIF orientation rotation
            var rotationAngle = 0f
            try {
                val exif = ExifInterface(tempFile.absolutePath)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                rotationAngle = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } catch (_: Throwable) {}

            tempFile.delete()

            val finalBitmap = if (rotationAngle != 0f) {
                val matrix = Matrix().apply { postRotate(rotationAngle) }
                Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.width, decodedBitmap.height, matrix, true)
            } else {
                decodedBitmap
            }

            // 5. Compress into target file
            FileOutputStream(targetFile).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }

            targetFile.absolutePath
        } catch (t: Throwable) {
            t.printStackTrace()
            // Final safety fallback: try direct copy if temp file exists
            if (tempFile.exists() && tempFile.length() > 0L) {
                try {
                    tempFile.copyTo(targetFile, overwrite = true)
                    tempFile.delete()
                    return@withContext targetFile.absolutePath
                } catch (_: Throwable) {}
            }
            tempFile.delete()
            null
        }
    }
}
