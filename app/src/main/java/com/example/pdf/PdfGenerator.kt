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
import android.text.TextUtils
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.DossierProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun getBookmanTypeface(context: Context, style: Int = Typeface.NORMAL): Typeface {
        val base = try {
            ResourcesCompat.getFont(context, R.font.bookman_old_style) ?: Typeface.SERIF
        } catch (_: Exception) {
            Typeface.SERIF
        }
        return Typeface.create(base, style)
    }

    private data class TableFieldEntry(
        val label: String,
        val value: String,
        val isMultiLine: Boolean = false
    ) {
        val isSmall: Boolean
            get() {
                if (isMultiLine || value.contains("\n")) return false
                if (value.length > 32) return false
                if (label.length > 28) return false
                val words = label.split(Regex("\\s+")).filter { it.isNotEmpty() }
                if (words.any { it.length > 14 }) return false
                return true
            }
    }

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
        val pdfFile = File(pdfDir, "${cleanName}_${System.currentTimeMillis()}.pdf")

        val document = PdfDocument()

        // Page 1: Main Structured Profile Dossier
        val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page1 = document.startPage(pageInfo1)
        renderDossier(context, page1.canvas, profile, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat())
        document.finishPage(page1)

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        pdfFile
    }

    suspend fun savePdfToDownloads(context: Context, file: File, displayName: String): Uri? = withContext(Dispatchers.IO) {
        val cleanBase = displayName.trim()
            .replace("\\s+".toRegex(), "_")
            .filter { it.isLetterOrDigit() || it == '_' || it == '.' }
        val fileName = if (cleanBase.endsWith(".pdf", ignoreCase = true)) cleanBase else "$cleanBase.pdf"

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
        // Pure White Background
        canvas.drawColor(Color.WHITE)

        val headerColor = try {
            Color.parseColor(profile.headerColorHex)
        } catch (_: Exception) {
            Color.parseColor("#102E56")
        }

        val borderColor = Color.parseColor("#CBD5E1")
        val labelColor = Color.parseColor("#0F172A")
        val valueColor = Color.parseColor("#1E293B")
        val watermarkColor = Color.parseColor("#0C000000") // clean subtle transparent black, no shadow

        val bookmanBase = getBookmanTypeface(context, Typeface.NORMAL)
        val bookmanRegular = Typeface.create(bookmanBase, Typeface.NORMAL)
        val bookmanBold = Typeface.create(bookmanBase, Typeface.BOLD)
        val bookmanBoldItalic = Typeface.create(bookmanBase, Typeface.BOLD_ITALIC)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

        val watermarkText = if (profile.watermarkText.isNotBlank() && profile.watermarkText != "by MMC") {
            profile.watermarkText
        } else {
            "MMC"
        }
        // 1. Watermark in background: Clean fill, no stroke or shadow color
        drawWatermark(canvas, width, height, watermarkText, watermarkColor, bookmanBoldItalic)

        // Margins
        val marginX = 36f
        val marginTop = 26f
        val contentWidth = width - (marginX * 2)

        // 2. Top Header Banner
        val bannerHeight = 40f
        paint.color = headerColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(marginX, marginTop, width - marginX, marginTop + bannerHeight, paint)

        // Top Banner Text
        paint.color = Color.WHITE
        paint.typeface = bookmanBold
        paint.textSize = 17f
        paint.textAlign = Paint.Align.CENTER
        val bannerText = profile.fullName.ifBlank { "PROFILE DOSSIER" }.uppercase()
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
        val photoHeight = 240f
        val badgeHeight = 28f
        val phoneNumbers = if (profile.displayPhotoNumber.isNotBlank()) {
            profile.displayPhotoNumber.split(Regex("[\n,/]+")).map { it.trim() }.filter { it.isNotBlank() }
        } else emptyList()
        val hasPhoneUnderPhoto = phoneNumbers.isNotEmpty()
        val phoneBoxHeight = when {
            !hasPhoneUnderPhoto -> 0f
            phoneNumbers.size == 1 -> 24f
            phoneNumbers.size == 2 -> 34f
            else -> 44f
        }
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

        // Photo Area
        val photoRect = RectF(
            photoCardLeft + photoPadding,
            photoCardTop + photoPadding,
            photoCardLeft + photoPadding + photoWidth,
            photoCardTop + photoPadding + photoHeight
        )

        val photoBitmap = loadBitmap(context, profile.photoUri, (photoWidth * 2).toInt(), (photoHeight * 2).toInt())
        if (photoBitmap != null) {
            drawCenterCropBitmap(canvas, photoBitmap, photoRect)
        } else {
            // Placeholder box with initials
            paint.color = Color.parseColor("#EDF2F7")
            paint.style = Paint.Style.FILL
            canvas.drawRect(photoRect, paint)

            paint.color = Color.parseColor("#A0AEC0")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(photoRect, paint)

            paint.style = Paint.Style.FILL
            paint.color = headerColor
            paint.textSize = 36f
            paint.typeface = bookmanBold
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
            paint.typeface = bookmanRegular
            canvas.drawText("PHOTO", photoRect.centerX(), initY + 28f, paint)
        }

        // Left Name Banner under Photo
        val badgeTop = photoCardTop + photoPadding + photoHeight
        paint.color = headerColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(photoCardLeft, badgeTop, photoCardLeft + leftCardWidth, badgeTop + badgeHeight, paint)

        paint.color = Color.WHITE
        paint.typeface = bookmanBold
        paint.textSize = 10.5f
        paint.textAlign = Paint.Align.CENTER
        val badgeTextY = badgeTop + (badgeHeight / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        val displayName = profile.fullName.ifBlank { "FULL NAME" }.uppercase()
        canvas.drawText(displayName, photoCardLeft + (leftCardWidth / 2f), badgeTextY, paint)

        // Left Phone Number box (ONLY if filled!)
        if (hasPhoneUnderPhoto) {
            val phoneTop = badgeTop + badgeHeight
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL
            canvas.drawRect(photoCardLeft, phoneTop, photoCardLeft + leftCardWidth, phoneTop + phoneBoxHeight, paint)

            paint.color = borderColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(photoCardLeft, phoneTop, photoCardLeft + leftCardWidth, phoneTop + phoneBoxHeight, paint)

            paint.color = Color.parseColor("#111827")
            paint.typeface = bookmanRegular
            if (phoneNumbers.size == 1) {
                paint.textSize = 10.5f
                paint.textAlign = Paint.Align.CENTER
                val phoneY = phoneTop + (phoneBoxHeight / 2f) - ((paint.descent() + paint.ascent()) / 2f)
                canvas.drawText(phoneNumbers[0], photoCardLeft + (leftCardWidth / 2f), phoneY, paint)
            } else {
                paint.textSize = 8.5f
                paint.textAlign = Paint.Align.CENTER
                val lineHeight = 12f
                val startLineY = phoneTop + 11f
                phoneNumbers.take(3).forEachIndexed { idx, num ->
                    canvas.drawText(num, photoCardLeft + (leftCardWidth / 2f), startLineY + (idx * lineHeight), paint)
                }
            }
        }

        // --- RIGHT COLUMN: DYNAMIC TABLES (ONLY FILLED ITEMS ADDED) ---
        var currentY = startY
        val tableHeaderHeight = 22f
        val standardRowHeight = 24f
        val col1Width = 85f

        // SECTION 1: PERSONAL & CONTACT INFORMATION
        val personalEntries = mutableListOf<TableFieldEntry>()
        if (profile.gender.isNotBlank()) personalEntries.add(TableFieldEntry("Gender", profile.gender))
        if (profile.lgaState.isNotBlank()) personalEntries.add(TableFieldEntry("LGA/State", profile.lgaState))
        if (profile.phoneNumber.isNotBlank()) {
            val isMulti = profile.phoneNumber.contains("\n") || profile.phoneNumber.length > 25
            personalEntries.add(TableFieldEntry("Phone Number(s)", profile.phoneNumber, isMulti))
        }
        if (profile.alsoKnownAs.isNotBlank()) personalEntries.add(TableFieldEntry("Also Known As", profile.alsoKnownAs))
        if (profile.dateOfBirth.isNotBlank()) personalEntries.add(TableFieldEntry("Date of Birth", profile.dateOfBirth))

        for (pf in profile.getPersonalInfoList()) {
            if (pf.label.isNotBlank() && pf.value.isNotBlank()) {
                val isMulti = pf.value.contains("\n") || pf.value.length > 40
                personalEntries.add(TableFieldEntry(pf.label, pf.value, isMulti))
            }
        }

        if (personalEntries.isNotEmpty()) {
            drawTableHeader(canvas, rightTableX, currentY, rightTableWidth, tableHeaderHeight, "PERSONAL & CONTACT INFORMATION", headerColor, bookmanBold)
            currentY += tableHeaderHeight

            currentY = drawSectionFields(
                canvas, textPaint, rightTableX, currentY, rightTableWidth,
                personalEntries, borderColor, labelColor, valueColor, standardRowHeight,
                bookmanBold, bookmanRegular
            )
            currentY += 12f
        }

        // SECTION 2: SOCIAL MEDIA
        val socialEntries = mutableListOf<TableFieldEntry>()
        if (profile.facebook.isNotBlank()) socialEntries.add(TableFieldEntry("Facebook", profile.facebook))
        if (profile.twitter.isNotBlank()) socialEntries.add(TableFieldEntry("Twitter / X", profile.twitter))
        if (profile.instagram.isNotBlank()) socialEntries.add(TableFieldEntry("Instagram", profile.instagram))
        if (profile.youtube.isNotBlank()) socialEntries.add(TableFieldEntry("YouTube", profile.youtube))
        if (profile.otherSocialMedia.isNotBlank()) socialEntries.add(TableFieldEntry("Other Social", profile.otherSocialMedia))

        if (socialEntries.isNotEmpty()) {
            drawTableHeader(canvas, rightTableX, currentY, rightTableWidth, tableHeaderHeight, "SOCIAL MEDIA", headerColor, bookmanBold)
            currentY += tableHeaderHeight

            currentY = drawSectionFields(
                canvas, textPaint, rightTableX, currentY, rightTableWidth,
                socialEntries, borderColor, labelColor, valueColor, standardRowHeight,
                bookmanBold, bookmanRegular
            )
            currentY += 12f
        }

        // SECTION 3: OTHER INFORMATION & CUSTOM FIELDS
        val otherEntries = mutableListOf<TableFieldEntry>()
        if (profile.occupation.isNotBlank()) otherEntries.add(TableFieldEntry("Occupation", profile.occupation))
        if (profile.education.isNotBlank()) otherEntries.add(TableFieldEntry("Education", profile.education))
        if (profile.associatesPhoneNumbers.isNotBlank()) {
            val isMulti = profile.associatesPhoneNumbers.contains("\n") || profile.associatesPhoneNumbers.length > 32
            otherEntries.add(TableFieldEntry("Associates Phone Numbers", profile.associatesPhoneNumbers, isMulti))
        }

        for (cf in profile.getOtherInfoList()) {
            if (cf.label.isNotBlank() && cf.value.isNotBlank()) {
                val isMulti = cf.value.contains("\n") || cf.value.length > 40
                otherEntries.add(TableFieldEntry(cf.label, cf.value, isMulti))
            }
        }

        if (otherEntries.isNotEmpty()) {
            drawTableHeader(canvas, rightTableX, currentY, rightTableWidth, tableHeaderHeight, "OTHER INFORMATION", headerColor, bookmanBold)
            currentY += tableHeaderHeight

            currentY = drawSectionFields(
                canvas, textPaint, rightTableX, currentY, rightTableWidth,
                otherEntries, borderColor, labelColor, valueColor, standardRowHeight,
                bookmanBold, bookmanRegular
            )
        }
    }

    private fun drawTableHeader(canvas: Canvas, x: Float, y: Float, width: Float, height: Float, title: String, color: Int, typeface: Typeface) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        canvas.drawRect(x, y, x + width, y + height, paint)

        paint.apply {
            this.color = Color.WHITE
            this.typeface = typeface
            textSize = 9.5f
            textAlign = Paint.Align.LEFT
        }
        val textY = y + (height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(title, x + 8f, textY, paint)
    }

    private fun drawSectionFields(
        canvas: Canvas,
        textPaint: TextPaint,
        x: Float,
        startY: Float,
        tableWidth: Float,
        entries: List<TableFieldEntry>,
        borderColor: Int,
        labelColor: Int,
        valueColor: Int,
        standardRowHeight: Float,
        labelTypeface: Typeface,
        valueTypeface: Typeface
    ): Float {
        var currentY = startY
        val col1Width = 85f
        val halfWidth = tableWidth / 2f
        val colLabel = 72f
        val colVal = halfWidth - colLabel

        val labelTestPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f
            typeface = labelTypeface
        }

        var i = 0
        while (i < entries.size) {
            val current = entries[i]
            val next = if (i + 1 < entries.size) entries[i + 1] else null

            if (current.isSmall && next != null && next.isSmall) {
                // Two small fields in a row (4 columns)
                val l1Lines = wrapLabelText(current.label, labelTestPaint, colLabel - 8f)
                val l2Lines = wrapLabelText(next.label, labelTestPaint, colLabel - 8f)
                val maxLabelLines = maxOf(l1Lines.size, l2Lines.size)
                val rowHeight = if (maxLabelLines > 1) {
                    maxOf(standardRowHeight, maxLabelLines * 11.5f + 10f)
                } else {
                    standardRowHeight
                }

                drawGridRow4Cols(
                    canvas, x, currentY,
                    colLabel, colVal, colLabel, colVal, rowHeight,
                    current.label, current.value,
                    next.label, next.value,
                    borderColor, labelColor, valueColor,
                    labelTypeface, valueTypeface
                )
                currentY += rowHeight
                i += 2
            } else {
                // Single field in a row (2 columns)
                labelTestPaint.textSize = 9f
                val labelLines = wrapLabelText(current.label, labelTestPaint, col1Width - 10f)
                val isMulti = current.isMultiLine || current.value.contains("\n") || current.value.length > 40
                if (isMulti) {
                    val labelMinH = labelLines.size * 12.5f + 12f
                    val minH = maxOf(26f, labelMinH)
                    val rowH = calculateRowHeight(current.value, tableWidth - col1Width, 9.5f, minH, valueTypeface)
                    drawGridRow2ColsMultiLine(
                        canvas, textPaint, x, currentY,
                        col1Width, tableWidth - col1Width, rowH,
                        current.label, current.value,
                        borderColor, labelColor, valueColor,
                        labelTypeface, valueTypeface
                    )
                    currentY += rowH
                } else {
                    val rowH = if (labelLines.size > 1) {
                        maxOf(standardRowHeight, labelLines.size * 12.5f + 10f)
                    } else {
                        standardRowHeight
                    }
                    drawGridRow2Cols(
                        canvas, x, currentY,
                        col1Width, tableWidth - col1Width, rowH,
                        current.label, current.value,
                        borderColor, labelColor, valueColor,
                        labelTypeface, valueTypeface
                    )
                    currentY += rowH
                }
                i += 1
            }
        }
        return currentY
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
        valColor: Int,
        labelTypeface: Typeface,
        valueTypeface: Typeface
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(x, y, x + w1, y + height, paint)
        canvas.drawRect(x + w1, y, x + w1 + w2, y + height, paint)
        canvas.drawRect(x + w1 + w2, y, x + w1 + w2 + w3, y + height, paint)
        canvas.drawRect(x + w1 + w2 + w3, y, x + w1 + w2 + w3 + w4, y + height, paint)

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f
            typeface = labelTypeface
            textAlign = Paint.Align.LEFT
        }

        // Label 1 (breaks to next line at blank spaces if it doesn't fit in w1)
        textPaint.color = labelColor
        textPaint.typeface = labelTypeface
        val l1Lines = wrapLabelText(label1, textPaint, maxOf(10f, w1 - 8f))
        drawWrappedTextLines(canvas, l1Lines, x + 5f, y, height, textPaint, 11f)

        // Value 1
        textPaint.color = valColor
        textPaint.typeface = valueTypeface
        val textY = y + (height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        val v1 = TextUtils.ellipsize(val1, textPaint, maxOf(10f, w2 - 8f), TextUtils.TruncateAt.END).toString()
        canvas.drawText(v1, x + w1 + 5f, textY, textPaint)

        // Label 2 (breaks to next line at blank spaces if it doesn't fit in w3)
        textPaint.color = labelColor
        textPaint.typeface = labelTypeface
        val l2Lines = wrapLabelText(label2, textPaint, maxOf(10f, w3 - 8f))
        drawWrappedTextLines(canvas, l2Lines, x + w1 + w2 + 5f, y, height, textPaint, 11f)

        // Value 2
        textPaint.color = valColor
        textPaint.typeface = valueTypeface
        val v2 = TextUtils.ellipsize(val2, textPaint, maxOf(10f, w4 - 8f), TextUtils.TruncateAt.END).toString()
        canvas.drawText(v2, x + w1 + w2 + w3 + 5f, textY, textPaint)
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
        valColor: Int,
        labelTypeface: Typeface,
        valueTypeface: Typeface
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(x, y, x + w1, y + height, paint)
        canvas.drawRect(x + w1, y, x + w1 + w2, y + height, paint)

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f
            typeface = labelTypeface
            textAlign = Paint.Align.LEFT
        }

        // Label: breaks to next line at blank spaces if it doesn't fit in w1
        textPaint.color = labelColor
        textPaint.typeface = labelTypeface
        val labelLines = wrapLabelText(label, textPaint, maxOf(10f, w1 - 10f))
        drawWrappedTextLines(canvas, labelLines, x + 6f, y, height, textPaint, 12f)

        // Value
        textPaint.color = valColor
        textPaint.typeface = valueTypeface
        val textY = y + (height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        val v = TextUtils.ellipsize(value, textPaint, maxOf(10f, w2 - 10f), TextUtils.TruncateAt.END).toString()
        canvas.drawText(v, x + w1 + 6f, textY, textPaint)
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
        valColor: Int,
        labelTypeface: Typeface,
        valueTypeface: Typeface
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(x, y, x + w1, y + height, paint)
        canvas.drawRect(x + w1, y, x + w1 + w2, y + height, paint)

        // Label: breaks to next line at blank spaces if it doesn't fit in w1
        textPaint.color = labelColor
        textPaint.typeface = labelTypeface
        textPaint.textSize = 9f
        textPaint.textAlign = Paint.Align.LEFT
        val labelLines = wrapLabelText(label, textPaint, maxOf(10f, w1 - 10f))
        drawWrappedTextLines(canvas, labelLines, x + 6f, y, height, textPaint, 12f)

        textPaint.color = valColor
        textPaint.typeface = valueTypeface
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
        val valueTopPadding = maxOf(4f, (height - layout.height) / 2f)
        canvas.translate(x + w1 + valuePadding, y + valueTopPadding)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun wrapLabelText(label: String, paint: Paint, maxWidth: Float): List<String> {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) return listOf("")
        val rawLines = trimmed.split("\n")
        val result = mutableListOf<String>()

        for (rawLine in rawLines) {
            if (paint.measureText(rawLine) <= maxWidth) {
                result.add(rawLine)
                continue
            }

            val words = rawLine.split(Regex("\\s+")).filter { it.isNotEmpty() }
            if (words.isEmpty()) continue

            var currentLine = ""
            for (word in words) {
                if (currentLine.isEmpty()) {
                    currentLine = word
                } else {
                    val candidate = "$currentLine $word"
                    if (paint.measureText(candidate) <= maxWidth) {
                        currentLine = candidate
                    } else {
                        result.add(currentLine)
                        currentLine = word
                    }
                }
            }
            if (currentLine.isNotEmpty()) {
                result.add(currentLine)
            }
        }
        return if (result.isEmpty()) listOf(trimmed) else result
    }

    private fun drawWrappedTextLines(
        canvas: Canvas,
        lines: List<String>,
        x: Float,
        y: Float,
        cellHeight: Float,
        paint: TextPaint,
        lineSpacing: Float
    ) {
        if (lines.isEmpty()) return
        if (lines.size == 1) {
            val textY = y + (cellHeight / 2f) - ((paint.descent() + paint.ascent()) / 2f)
            canvas.drawText(lines[0], x, textY, paint)
            return
        }
        val totalTextHeight = (lines.size - 1) * lineSpacing - paint.ascent() + paint.descent()
        var currentY = y + maxOf(3f, (cellHeight - totalTextHeight) / 2f) - paint.ascent()
        for (line in lines) {
            canvas.drawText(line, x, currentY, paint)
            currentY += lineSpacing
        }
    }

    private fun calculateRowHeight(text: String, width: Float, textSize: Float, minHeight: Float, typeface: Typeface): Float {
        val textPaint = TextPaint().apply {
            this.textSize = textSize
            this.typeface = typeface
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

    private fun drawWatermark(
        canvas: Canvas,
        width: Float,
        height: Float,
        text: String = "MMC",
        color: Int,
        typeface: Typeface
    ) {
        canvas.save()
        canvas.translate(width * 0.54f, height * 0.58f)
        canvas.rotate(-32f)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = 210f
            this.typeface = typeface
            textAlign = Paint.Align.CENTER
            style = Paint.Style.FILL
            clearShadowLayer()
        }
        canvas.drawText(text, 0f, 60f, paint)

        canvas.restore()
    }

    private fun loadBitmap(context: Context, uriString: String?, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (uriString.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = if (uri.scheme == "file") {
                File(uri.path ?: "").inputStream()
            } else if (uri.scheme == "content") {
                context.contentResolver.openInputStream(uri)
            } else {
                File(uriString).inputStream()
            } ?: return null

            inputStream.use { stream ->
                val bytes = stream.readBytes()
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
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
            val cropWidth = (srcHeight * destRatio).toInt()
            val left = ((srcWidth - cropWidth) / 2).toInt()
            srcRect = Rect(left, 0, left + cropWidth, bitmap.height)
        } else {
            val cropHeight = (srcWidth / destRatio).toInt()
            val top = ((srcHeight - cropHeight) / 2).toInt()
            srcRect = Rect(0, top, bitmap.width, top + cropHeight)
        }

        canvas.drawBitmap(bitmap, srcRect, destRect, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
    }
}
