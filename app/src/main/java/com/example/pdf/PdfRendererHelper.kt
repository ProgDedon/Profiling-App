package com.example.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object PdfRendererHelper {

    suspend fun renderPdfPages(file: File, targetWidth: Int = 1000): List<Bitmap> = withContext(Dispatchers.IO) {
        val bitmaps = mutableListOf<Bitmap>()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null

        try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)

            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val scale = targetWidth.toFloat() / page.width.toFloat()
                val targetHeight = (page.height * scale).toInt()

                val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                bitmaps.add(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                renderer?.close()
                pfd?.close()
            } catch (_: Exception) {}
        }

        bitmaps
    }
}
