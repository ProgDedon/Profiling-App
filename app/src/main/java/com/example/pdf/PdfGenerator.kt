package com.example.pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.data.model.DossierProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object PdfGenerator {

    // Standard A4 Landscape in points (72 DPI)
    const val PAGE_WIDTH = 842
    const val PAGE_HEIGHT = 595

    suspend fun generatePdf(context: Context, profile: DossierProfile): File = withContext(Dispatchers.IO) {
        val pdfDir = File(context.cacheDir, "pdfs")
        if (!pdfDir.exists()) pdfDir.mkdirs()

        val cleanName = profile.fullName.trim()
            .replace("\\s+".toRegex(), "_")
            .filter { it.isLetterOrDigit() || it == '_' }
            .ifEmpty { "Profile" }
        val pdfFile = File(pdfDir, "${cleanName}_Dossier_${System.currentTimeMillis()}.pdf")

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        renderDossier(context, canvas, profile, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat())

        document.finishPage(page)

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        pdfFile
    }

    suspend fun savePdfToDownloads(context: Context, file: File, displayName: String): Uri? = withContext(Dispatchers.IO) {
        val fileName = if (displayName.endsWith(".pdf", ignoreCase = true)) displayName else "$displayName.pdf"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Dossiers")
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    file.inputStream().use { input ->
                        input.copyTo(out)
                    }
                }
            }
            uri
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val subDir = File(downloadsDir, "Dossiers")
            if (!subDir.exists()) subDir.mkdirs()
            val destFile = File(subDir, fileName)
            file.copyTo(destFile, overwrite = true)
            Uri.fromFile(destFile)
        }
    }

    fun renderDossier(
        context: Context,
        canvas: Canvas,
        profile: DossierProfile,
        width: Float,
        height: Float
    ) {
        // Background - Pure White
        canvas.drawColor(Color.WHITE)

        val headerColor = try {
            Color.parseColor(profile.headerColorHex)
        } catch (_: Exception) {
            Color.parseColor("#102E56")
        }

        val borderColor = Color.parseColor("#CBD5E1")
        val labelColor = Color.parseColor("#0F172A")
        val valueColor = Color.parseColor("#1E293B")
        val watermarkColor = Color.parseColor("#0F000000") // 6% black

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

        // 1. Watermark in background
        drawWatermark(canvas, width, height, profile.watermarkText, watermarkColor)

        // Margins
        val marginX = 36f
        val marginTop = 26f
        val contentWidth = width - (marginX * 2)

        // 2. Top Banner
        val bannerHeight = 40f
        paint.color = headerColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(marginX, marginTop, width - marginX, marginTop + bannerHeight, paint)

        // Top Banner Text
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textSize = 17f
        paint.textAlign = Paint.Align.CENTER
        val bannerText = profile.fullName.ifBlank { "FULL NAME" }.uppercase()
        val textY = marginTop + (bannerHeight / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(bannerText, width / 2f, textY, paint)

        // 3. Layout Dimensions for Left Card & Right Tables
        val startY = marginTop + bannerHeight + 20f
        val leftCardWidth = 200f
        val gap = 18f
        val rightTableX = marginX + leftCardWidth + gap
        val rightTableWidth = contentWidth - leftCardWidth - gap

        // --- LEFT COLUMN: PHOTO & BADGE ---
        val photoCardLeft = marginX
        val photoCardTop = startY
        val photoPadding = 10f
        val photoWidth = leftCardWidth - (photoPadding * 2)
        val photoHeight = 250f
        val badgeHeight = 28f
        val phoneBoxHeight = 26f
        val totalCardHeight = photoPadding + photoHeight + badgeHeight + phoneBoxHeight

        // Left Card Outer Frame
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRect(photoCardLeft, photoCardTop, photoCardLeft + leftCardWidth, photoCardTop + totalCardHeight, paint)

        // Outer Border
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(photoCardLeft, photoCardTop, photoCardLeft + leftCardWidth, photoCardTop + totalCardHeight, paint)

        // Photo Canvas Area
        val photoRect = RectF(
            photoCardLeft + photoPadding,
            photoCardTop + photoPadding,
            photoCardLeft + photoPadding + photoWidth,
            photoCardTop + photoPadding + photoHeight
        )

        // Draw profile photo or styled placeholder
        val photoBitmap = loadBitmap(context, profile.photoUri, (photoWidth * 2).toInt(), (photoHeight * 2).toInt())
        if (photoBitmap != null) {
            drawCenterCropBitmap(canvas, photoBitmap, photoRect)
        } else {
            // Draw placeholder avatar with soft gradient and initials
            paint.color = Color.parseColor("#EDF2F7")
            paint.style = Paint.Style.FILL
            canvas.drawRect(photoRect, paint)

            paint.color = Color.parseColor("#A0AEC0")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(photoRect, paint)

            // Initials or Silhouette
            paint.style = Paint.Style.FILL
            paint.color = headerColor
            paint.textSize = 36f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            val initials = profile.fullName.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.take(1).uppercase() }
                .ifEmpty { "?" }
            val initY = photoRect.centerY() - ((paint.descent() + paint.ascent()) / 2f)
            canvas.drawText(initials, photoRect.centerX(), initY, paint)

            paint.textSize = 10f
            paint.color = Color.parseColor("#718096")
            canvas.drawText("PHOTO", photoRect.centerX(), initY + 28f, paint)
        }

        // Left Name Banner under Photo
        val badgeTop = photoCardTop + photoPadding + photoHeight
        paint.color = headerColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(photoCardLeft, badgeTop, photoCardLeft + leftCardWidth, badgeTop + badgeHeight, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textSize = 10.5f
        paint.textAlign = Paint.Align.CENTER
        val badgeTextY = badgeTop + (badgeHeight / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        val displayName = profile.fullName.ifBlank { "FULL NAME" }.uppercase()
        canvas.drawText(displayName, photoCardLeft + (leftCardWidth / 2f), badgeTextY, paint)

        // Left Phone Number box
        val phoneTop = badgeTop + badgeHeight
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRect(photoCardLeft, phoneTop, photoCardLeft + leftCardWidth, phoneTop + phoneBoxHeight, paint)

        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(photoCardLeft, phoneTop, photoCardLeft + leftCardWidth, phoneTop + phoneBoxHeight, paint)

        paint.color = Color.parseColor("#111827")
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 11f
        paint.textAlign = Paint.Align.CENTER
        val phoneY = phoneTop + (phoneBoxHeight / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        val displayPhone = profile.displayPhotoNumber.ifBlank { profile.phoneNumber }
        canvas.drawText(displayPhone, photoCardLeft + (leftCardWidth / 2f), phoneY, paint)

        // --- RIGHT COLUMN: 3 TABLES ---
        var currentY = startY
        val tableHeaderHeight = 22f
        val rowHeight = 24f

        // ==========================================
        // SECTION 1: PERSONAL & CONTACT INFORMATION
        // ==========================================
        drawTableHeader(canvas, rightTableX, currentY, rightTableWidth, tableHeaderHeight, "PERSONAL & CONTACT INFORMATION", headerColor)
        currentY += tableHeaderHeight

        // Row 1: Gender | Male | LGA/State: | Kontagori, Niger State
        val col1Width = 85f
        val col2Width = 160f
        val col3Width = 95f
        val col4Width = rightTableWidth - col1Width - col2Width - col3Width

        drawGridRow4Cols(
            canvas, rightTableX, currentY,
            col1Width, col2Width, col3Width, col4Width, rowHeight,
            "Gender:", profile.gender.ifBlank { "—" },
            "LGA/State:", profile.lgaState.ifBlank { "—" },
            borderColor, labelColor, valueColor
        )
        currentY += rowHeight

        // Row 2: Phone Number: | 07062270031 | Also Known As | Sardaunan Samari
        drawGridRow4Cols(
            canvas, rightTableX, currentY,
            col1Width, col2Width, col3Width, col4Width, rowHeight,
            "Phone Number:", profile.phoneNumber.ifBlank { "—" },
            "Also Known As", profile.alsoKnownAs.ifBlank { "—" },
            borderColor, labelColor, valueColor
        )
        currentY += rowHeight

        // Row 3: Date of Birth | 18th August, 1992
        drawGridRow2Cols(
            canvas, rightTableX, currentY,
            col1Width, rightTableWidth - col1Width, rowHeight,
            "Date of Birth", profile.dateOfBirth.ifBlank { "—" },
            borderColor, labelColor, valueColor
        )
        currentY += rowHeight

        // Gap
        currentY += 12f

        // ==========================================
        // SECTION 2: SOCIAL MEDIA
        // ==========================================
        drawTableHeader(canvas, rightTableX, currentY, rightTableWidth, tableHeaderHeight, "SOCIAL MEDIA", headerColor)
        currentY += tableHeaderHeight

        // Row 1: Facebook | value
        drawGridRow2Cols(
            canvas, rightTableX, currentY,
            col1Width, rightTableWidth - col1Width, rowHeight,
            "Facebook", profile.facebook.ifBlank { "—" },
            borderColor, labelColor, valueColor
        )
        currentY += rowHeight

        if (profile.otherSocialMedia.isNotBlank()) {
            drawGridRow2Cols(
                canvas, rightTableX, currentY,
                col1Width, rightTableWidth - col1Width, rowHeight,
                "Other Media", profile.otherSocialMedia,
                borderColor, labelColor, valueColor
            )
            currentY += rowHeight
        }

        // Gap
        currentY += 12f

        // ==========================================
        // SECTION 3: OTHER INFORMATION
        // ==========================================
        drawTableHeader(canvas, rightTableX, currentY, rightTableWidth, tableHeaderHeight, "OTHER INFORMATION", headerColor)
        currentY += tableHeaderHeight

        // Row 1: Occupation
        val occHeight = calculateRowHeight(profile.occupation.ifBlank { "—" }, rightTableWidth - col1Width, 10f, 26f)
        drawGridRow2ColsMultiLine(
            canvas, textPaint, rightTableX, currentY,
            col1Width, rightTableWidth - col1Width, occHeight,
            "Occupation", profile.occupation.ifBlank { "—" },
            borderColor, labelColor, valueColor
        )
        currentY += occHeight

        // Row 2: Education
        val eduHeight = calculateRowHeight(profile.education.ifBlank { "—" }, rightTableWidth - col1Width, 10f, 26f)
        drawGridRow2ColsMultiLine(
            canvas, textPaint, rightTableX, currentY,
            col1Width, rightTableWidth - col1Width, eduHeight,
            "Education", profile.education.ifBlank { "—" },
            borderColor, labelColor, valueColor
        )
        currentY += eduHeight

        // Row 3: Associates Phone Numbers
        val associatesText = profile.associatesPhoneNumbers.ifBlank { "—" }
        val assocHeight = calculateRowHeight(associatesText, rightTableWidth - col1Width, 10f, 48f)
        drawGridRow2ColsMultiLine(
            canvas, textPaint, rightTableX, currentY,
            col1Width, rightTableWidth - col1Width, assocHeight,
            "Associates Phone\nNumbers", associatesText,
            borderColor, labelColor, valueColor
        )
    }

    private fun drawTableHeader(canvas: Canvas, x: Float, y: Float, width: Float, height: Float, title: String, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        canvas.drawRect(x, y, x + width, y + height, paint)

        paint.apply {
            this.color = Color.WHITE
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textSize = 9.5f
            textAlign = Paint.Align.LEFT
        }
        val textY = y + (height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(title, x + 8f, textY, paint)
    }

    private fun drawGridRow4Cols(
        canvas: Canvas,
        x: Float,
        y: Float,
        w1: Float,
        w2: Float,
        w3: Float,
        w4: Float,
        height: Float,
        label1: String,
        val1: String,
        label2: String,
        val2: String,
        borderColor: Int,
        labelColor: Int,
        valColor: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Draw Cell Borders
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(x, y, x + w1, y + height, paint)
        canvas.drawRect(x + w1, y, x + w1 + w2, y + height, paint)
        canvas.drawRect(x + w1 + w2, y, x + w1 + w2 + w3, y + height, paint)
        canvas.drawRect(x + w1 + w2 + w3, y, x + w1 + w2 + w3 + w4, y + height, paint)

        // Label 1
        paint.style = Paint.Style.FILL
        paint.color = labelColor
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textSize = 9f
        paint.textAlign = Paint.Align.LEFT
        val textY = y + (height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(label1, x + 6f, textY, paint)

        // Value 1
        paint.color = valColor
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        canvas.drawText(val1, x + w1 + 6f, textY, paint)

        // Label 2
        paint.color = labelColor
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        canvas.drawText(label2, x + w1 + w2 + 6f, textY, paint)

        // Value 2
        paint.color = valColor
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        canvas.drawText(val2, x + w1 + w2 + w3 + 6f, textY, paint)
    }

    private fun drawGridRow2Cols(
        canvas: Canvas,
        x: Float,
        y: Float,
        w1: Float,
        w2: Float,
        height: Float,
        label: String,
        value: String,
        borderColor: Int,
        labelColor: Int,
        valColor: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Cell borders
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(x, y, x + w1, y + height, paint)
        canvas.drawRect(x + w1, y, x + w1 + w2, y + height, paint)

        // Label
        paint.style = Paint.Style.FILL
        paint.color = labelColor
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textSize = 9f
        paint.textAlign = Paint.Align.LEFT
        val textY = y + (height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(label, x + 6f, textY, paint)

        // Value
        paint.color = valColor
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        canvas.drawText(value, x + w1 + 6f, textY, paint)
    }

    private fun drawGridRow2ColsMultiLine(
        canvas: Canvas,
        textPaint: TextPaint,
        x: Float,
        y: Float,
        w1: Float,
        w2: Float,
        height: Float,
        label: String,
        value: String,
        borderColor: Int,
        labelColor: Int,
        valColor: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Cell borders
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(x, y, x + w1, y + height, paint)
        canvas.drawRect(x + w1, y, x + w1 + w2, y + height, paint)

        // Draw multi-line or single-line label
        textPaint.color = labelColor
        textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        textPaint.textSize = 9f
        val labelLines = label.split("\n")
        var labelY = y + 14f
        for (line in labelLines) {
            canvas.drawText(line, x + 6f, labelY, textPaint)
            labelY += 12f
        }

        // Draw value with text wrap
        textPaint.color = valColor
        textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        val valuePadding = 6f
        val usableWidth = (w2 - (valuePadding * 2)).toInt().coerceAtLeast(10)

        val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(value, 0, value.length, textPaint, usableWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(2f, 1f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(value, textPaint, usableWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 2f, false)
        }

        canvas.save()
        canvas.translate(x + w1 + valuePadding, y + 6f)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun calculateRowHeight(text: String, width: Float, textSize: Float, minHeight: Float): Float {
        val textPaint = TextPaint().apply {
            this.textSize = textSize
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }
        val usableWidth = (width - 12f).toInt().coerceAtLeast(10)
        val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, textPaint, usableWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(2f, 1f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, textPaint, usableWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 2f, false)
        }
        val calculated = layout.height + 14f
        return calculated.coerceAtLeast(minHeight)
    }

    private fun drawWatermark(canvas: Canvas, width: Float, height: Float, text: String, color: Int) {
        if (text.isBlank()) return
        canvas.save()
        // Center rotation
        canvas.translate(width * 0.55f, height * 0.58f)
        canvas.rotate(-32f)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = 210f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
            textAlign = Paint.Align.CENTER
            style = Paint.Style.STROKE
            strokeWidth = 32f
        }
        canvas.drawText(text, 0f, 60f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#08000000") // Very soft fill
        canvas.drawText(text, 0f, 60f, paint)

        canvas.restore()
    }

    private fun loadBitmap(context: Context, uriString: String?, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (uriString.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = if (uri.scheme == "file") {
                File(uri.path ?: "").inputStream()
            } else {
                context.contentResolver.openInputStream(uri)
            } ?: return null

            inputStream.use { stream ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                // First decode bounds
                val bytes = stream.readBytes()
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

                var inSampleSize = 1
                if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                    val halfHeight = options.outHeight / 2
                    val halfWidth = options.outWidth / 2
                    while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                        inSampleSize *= 2
                    }
                }

                val decodeOpts = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun drawCenterCropBitmap(canvas: Canvas, bitmap: Bitmap, destRect: RectF) {
        val srcWidth = bitmap.width.toFloat()
        val srcHeight = bitmap.height.toFloat()
        val destWidth = destRect.width()
        val destHeight = destRect.height()

        val srcRatio = srcWidth / srcHeight
        val destRatio = destWidth / destHeight

        val srcRect: Rect
        if (srcRatio > destRatio) {
            // Bitmap is wider than destination
            val cropWidth = (srcHeight * destRatio).toInt()
            val left = ((srcWidth - cropWidth) / 2).toInt()
            srcRect = Rect(left, 0, left + cropWidth, bitmap.height)
        } else {
            // Bitmap is taller than destination
            val cropHeight = (srcWidth / destRatio).toInt()
            val top = ((srcHeight - cropHeight) / 2).toInt()
            srcRect = Rect(0, top, bitmap.width, top + cropHeight)
        }

        canvas.drawBitmap(bitmap, srcRect, destRect, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
    }
}
