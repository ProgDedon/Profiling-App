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
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.DossierProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

data class TableFieldEntry(
    val label: String,
    val value: String,
    val isMultiLine: Boolean = false
)

sealed class RenderRow {
    data class MultiLine(val entry: TableFieldEntry) : RenderRow()
    data class Pair(val entry1: TableFieldEntry, val entry2: TableFieldEntry) : RenderRow()
    data class Single(val entry: TableFieldEntry) : RenderRow()
}

data class SectionData(
    val title: String,
    val rows: List<RenderRow>
)

object PdfGenerator {

    fun getBookmanTypeface(context: Context, style: Int = Typeface.NORMAL): Typeface {
        return try {
            val baseTypeface = ResourcesCompat.getFont(context, R.font.bookman_old_style)
                ?: Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            Typeface.create(baseTypeface, style)
        } catch (_: Exception) {
            Typeface.create(Typeface.SERIF, style)
        }
    }

    suspend fun generatePdf(context: Context, profile: DossierProfile): File = withContext(Dispatchers.IO) {
        val pdfDir = File(context.filesDir, "generated_pdfs").apply { mkdirs() }
        val cleanName = profile.fullName.trim()
            .replace("\\s+".toRegex(), "_")
            .filter { it.isLetterOrDigit() || it == '_' }
            .ifEmpty { "Profile" }
        val pdfFile = File(pdfDir, "${cleanName}_${System.currentTimeMillis()}.pdf")

        val document = PdfDocument()

        // Standard A4 dimensions in points (72 DPI) - Landscape Orientation
        val pageWidth = 842
        val pageHeight = 595

        renderMultiPageDossier(context, document, profile, pageWidth, pageHeight)

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        pdfFile
    }

    private fun renderMultiPageDossier(
        context: Context,
        document: PdfDocument,
        profile: DossierProfile,
        pageWidth: Int,
        pageHeight: Int
    ) {
        val width = pageWidth.toFloat()
        val height = pageHeight.toFloat()

        val headerColor = try {
            Color.parseColor(profile.headerColorHex)
        } catch (_: Exception) {
            Color.parseColor("#102E56")
        }

        val borderColor = Color.parseColor("#CBD5E1")
        val labelColor = Color.parseColor("#0F172A")
        val valueColor = Color.parseColor("#1E293B")
        val watermarkColor = Color.parseColor("#0C000000")

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

        val marginX = 36f
        val startY = 24f
        val bannerHeight = 34f
        val contentWidth = width - (marginX * 2f) // 770f
        val maxY = height - 34f // 561f

        val sections = buildSections(profile)

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var currentPage = document.startPage(pageInfo)
        var canvas = currentPage.canvas

        fun drawPageBackgroundAndWatermark(c: Canvas) {
            c.drawColor(Color.WHITE)
            drawWatermark(c, width, height, watermarkText, watermarkColor, bookmanBoldItalic)
        }

        fun drawFooter(c: Canvas, pageNum: Int) {
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#94A3B8")
                typeface = bookmanRegular
                textSize = 8.5f
                textAlign = Paint.Align.CENTER
            }
            c.drawText("greymmc  •  Page $pageNum", width / 2f, height - 14f, footerPaint)
        }

        // Draw Page 1 base
        drawPageBackgroundAndWatermark(canvas)

        // Page 1 Banner
        paint.color = headerColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(marginX, startY, marginX + contentWidth, startY + bannerHeight, paint)

        paint.color = Color.WHITE
        paint.typeface = bookmanBold
        paint.textSize = 14f
        paint.textAlign = Paint.Align.CENTER
        val bannerText = profile.fullName.ifBlank { "DOSSIER PROFILE" }.uppercase()
        val bannerTextY = startY + (bannerHeight / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(bannerText, marginX + (contentWidth / 2f), bannerTextY, paint)

        // Left photo card and phone box
        val gap = 16f
        val leftCardWidth = 150f
        val contentTop = startY + bannerHeight + 12f
        val photoCardLeft = marginX
        val photoCardTop = contentTop
        val photoHeight = 180f

        // Photo Border Box
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRect(photoCardLeft, photoCardTop, photoCardLeft + leftCardWidth, photoCardTop + photoHeight, paint)

        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f
        canvas.drawRect(photoCardLeft, photoCardTop, photoCardLeft + leftCardWidth, photoCardTop + photoHeight, paint)

        val photoBitmap = profile.photoUri?.let { loadBitmap(context, it, leftCardWidth.toInt() * 2, photoHeight.toInt() * 2) }
        if (photoBitmap != null) {
            val photoRect = RectF(photoCardLeft + 1f, photoCardTop + 1f, photoCardLeft + leftCardWidth - 1f, photoCardTop + photoHeight - 1f)
            drawCenterCropBitmap(canvas, photoBitmap, photoRect)
        } else {
            val owlBitmap = try {
                BitmapFactory.decodeResource(context.resources, R.drawable.owl_logo_icon_1793603390711)
            } catch (_: Exception) { null }

            if (owlBitmap != null) {
                val emblemSize = 92f
                val emblemLeft = photoCardLeft + (leftCardWidth - emblemSize) / 2f
                val emblemTop = photoCardTop + 24f
                val emblemRect = RectF(emblemLeft, emblemTop, emblemLeft + emblemSize, emblemTop + emblemSize)
                drawCenterCropBitmap(canvas, owlBitmap, emblemRect)

                paint.color = Color.parseColor("#64748B")
                paint.typeface = bookmanRegular
                paint.textSize = 8.5f
                paint.textAlign = Paint.Align.CENTER
                val cx = photoCardLeft + (leftCardWidth / 2f)
                canvas.drawText("OFFICIAL DOSSIER", cx, photoCardTop + photoHeight - 16f, paint)
            } else {
                paint.color = Color.parseColor("#94A3B8")
                paint.style = Paint.Style.FILL
                val cx = photoCardLeft + (leftCardWidth / 2f)
                val cy = photoCardTop + 72f
                canvas.drawCircle(cx, cy, 28f, paint)
                val bodyRect = RectF(cx - 36f, cy + 34f, cx + 36f, cy + 82f)
                canvas.drawRoundRect(bodyRect, 18f, 18f, paint)

                paint.color = Color.parseColor("#64748B")
                paint.typeface = bookmanRegular
                paint.textSize = 10f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("NO PHOTO", cx, photoCardTop + photoHeight - 14f, paint)
            }
        }

        // Phone numbers box directly underneath photo
        val rawPhoneString = profile.displayPhotoNumber
        val phoneNumbers = rawPhoneString.split("\n", ",", "/").map { it.trim() }.filter { it.isNotBlank() }
        val phoneTop = photoCardTop + photoHeight + 8f

        if (phoneNumbers.isNotEmpty()) {
            val phoneBoxHeight = if (phoneNumbers.size == 1) 22f else (phoneNumbers.take(3).size * 13f + 8f)
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

        var currentTableX = photoCardLeft + leftCardWidth + gap
        var currentTableWidth = contentWidth - leftCardWidth - gap
        var currentY = contentTop

        val tableHeaderHeight = 22f
        val standardRowHeight = 22f

        fun startNewPage() {
            drawFooter(canvas, pageNumber)
            document.finishPage(currentPage)

            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            currentPage = document.startPage(pageInfo)
            canvas = currentPage.canvas

            drawPageBackgroundAndWatermark(canvas)

            // Continuation Banner
            paint.color = headerColor
            paint.style = Paint.Style.FILL
            canvas.drawRect(marginX, startY, marginX + contentWidth, startY + bannerHeight, paint)

            paint.color = Color.WHITE
            paint.typeface = bookmanBold
            paint.textSize = 13f
            paint.textAlign = Paint.Align.CENTER
            val continuationTitle = "${profile.fullName.ifBlank { "DOSSIER PROFILE" }.uppercase()} — PAGE $pageNumber"
            val contY = startY + (bannerHeight / 2f) - ((paint.descent() + paint.ascent()) / 2f)
            canvas.drawText(continuationTitle, marginX + (contentWidth / 2f), contY, paint)

            currentTableX = marginX
            currentTableWidth = contentWidth
            currentY = startY + bannerHeight + 12f
        }

        for (section in sections) {
            val firstRowHeight = if (section.rows.isNotEmpty()) {
                getRowHeight(section.rows[0], currentTableWidth, standardRowHeight, bookmanRegular)
            } else {
                standardRowHeight
            }

            if (currentY + tableHeaderHeight + firstRowHeight > maxY) {
                startNewPage()
            }

            drawTableHeader(
                canvas, currentTableX, currentY, currentTableWidth, tableHeaderHeight,
                section.title, headerColor, bookmanBold
            )
            currentY += tableHeaderHeight

            for (row in section.rows) {
                val rowHeight = getRowHeight(row, currentTableWidth, standardRowHeight, bookmanRegular)

                if (currentY + rowHeight > maxY) {
                    startNewPage()
                    drawTableHeader(
                        canvas, currentTableX, currentY, currentTableWidth, tableHeaderHeight,
                        "${section.title} (CONTINUED)", headerColor, bookmanBold
                    )
                    currentY += tableHeaderHeight
                }

                drawRow(
                    canvas, textPaint, currentTableX, currentY, currentTableWidth,
                    row, rowHeight, borderColor, labelColor, valueColor,
                    bookmanBold, bookmanRegular
                )
                currentY += rowHeight
            }

            currentY += 10f
        }

        drawFooter(canvas, pageNumber)
        document.finishPage(currentPage)
    }

    private fun drawTableHeader(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        color: Int,
        typeface: Typeface
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        canvas.drawRect(x, y, x + width, y + height, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            this.typeface = typeface
            textSize = 9.5f
            textAlign = Paint.Align.LEFT
        }
        val textY = y + (height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(title, x + 8f, textY, textPaint)
    }

    fun buildSections(profile: DossierProfile): List<SectionData> {
        val personalEntries = mutableListOf<TableFieldEntry>()
        if (profile.gender.isNotBlank()) personalEntries.add(TableFieldEntry("Gender", profile.gender))
        if (profile.lgaState.isNotBlank()) personalEntries.add(TableFieldEntry("LGA/State", profile.lgaState))
        if (profile.address.isNotBlank()) {
            val isMulti = profile.address.contains("\n") || profile.address.length > 25
            personalEntries.add(TableFieldEntry("Address", profile.address, isMulti))
        }
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

        val socialEntries = mutableListOf<TableFieldEntry>()
        if (profile.facebook.isNotBlank()) socialEntries.add(TableFieldEntry("Facebook", profile.facebook))
        if (profile.twitter.isNotBlank()) socialEntries.add(TableFieldEntry("Twitter / X", profile.twitter))
        if (profile.instagram.isNotBlank()) socialEntries.add(TableFieldEntry("Instagram", profile.instagram))
        if (profile.tiktok.isNotBlank()) socialEntries.add(TableFieldEntry("TikTok", profile.tiktok))
        if (profile.youtube.isNotBlank()) socialEntries.add(TableFieldEntry("YouTube", profile.youtube))
        if (profile.email.isNotBlank()) {
            val isMulti = profile.email.length > 28
            socialEntries.add(TableFieldEntry("Email", profile.email, isMulti))
        }
        if (profile.websites.isNotBlank()) {
            val isMulti = profile.websites.contains("\n") || profile.websites.length > 28
            socialEntries.add(TableFieldEntry("Websites", profile.websites, isMulti))
        }
        if (profile.otherSocialMedia.isNotBlank()) socialEntries.add(TableFieldEntry("Other Social", profile.otherSocialMedia))

        val otherEntries = mutableListOf<TableFieldEntry>()
        if (profile.occupation.isNotBlank()) otherEntries.add(TableFieldEntry("Occupation/ Role", profile.occupation))
        if (profile.education.isNotBlank()) otherEntries.add(TableFieldEntry("Education", profile.education))
        if (profile.associatesPhoneNumbers.isNotBlank()) {
            val isMulti = profile.associatesPhoneNumbers.contains("\n") || profile.associatesPhoneNumbers.length > 30
            otherEntries.add(TableFieldEntry("Associates Phone", profile.associatesPhoneNumbers, isMulti))
        }

        for (cf in profile.getOtherInfoList()) {
            if (cf.label.isNotBlank() && cf.value.isNotBlank()) {
                val isMulti = cf.value.contains("\n") || cf.value.length > 40
                otherEntries.add(TableFieldEntry(cf.label, cf.value, isMulti))
            }
        }

        val sections = mutableListOf<SectionData>()
        if (personalEntries.isNotEmpty()) {
            sections.add(SectionData("PERSONAL & CONTACT INFORMATION", buildRows(personalEntries)))
        }
        if (socialEntries.isNotEmpty()) {
            sections.add(SectionData("SOCIAL MEDIA", buildRows(socialEntries)))
        }
        if (otherEntries.isNotEmpty()) {
            sections.add(SectionData("OTHER INFORMATION", buildRows(otherEntries)))
        }
        return sections
    }

    fun buildRows(entries: List<TableFieldEntry>): List<RenderRow> {
        val rows = mutableListOf<RenderRow>()
        var i = 0
        while (i < entries.size) {
            val entry1 = entries[i]
            if (entry1.isMultiLine) {
                rows.add(RenderRow.MultiLine(entry1))
                i++
            } else if (i + 1 < entries.size && !entries[i + 1].isMultiLine) {
                rows.add(RenderRow.Pair(entry1, entries[i + 1]))
                i += 2
            } else {
                rows.add(RenderRow.Single(entry1))
                i++
            }
        }
        return rows
    }

    fun estimatePageCount(profile: DossierProfile, pageHeight: Float = 595f): Int {
        val sections = buildSections(profile)
        val maxY = pageHeight - 34f
        var currentY = 24f + 34f + 12f
        var pageCount = 1

        for (section in sections) {
            val tableHeaderHeight = 22f
            currentY += tableHeaderHeight
            for (row in section.rows) {
                val rowHeight = when (row) {
                    is RenderRow.MultiLine -> 34f
                    else -> 22f
                }
                if (currentY + rowHeight > maxY) {
                    pageCount++
                    currentY = 24f + 34f + 12f + tableHeaderHeight + rowHeight
                } else {
                    currentY += rowHeight
                }
            }
            currentY += 10f
        }
        return pageCount
    }

    private fun getRowHeight(
        row: RenderRow,
        tableWidth: Float,
        standardRowHeight: Float,
        regularTypeface: Typeface
    ): Float {
        return when (row) {
            is RenderRow.MultiLine -> {
                val labelColWidth = if (tableWidth > 500f) 100f else 85f
                calculateRowHeight(row.entry.value, tableWidth - labelColWidth, 12f, standardRowHeight, regularTypeface)
            }
            is RenderRow.Pair -> standardRowHeight
            is RenderRow.Single -> standardRowHeight
        }
    }

    private fun drawRow(
        canvas: Canvas,
        textPaint: TextPaint,
        x: Float,
        y: Float,
        totalWidth: Float,
        row: RenderRow,
        height: Float,
        borderColor: Int,
        labelColor: Int,
        valueColor: Int,
        boldTypeface: Typeface,
        regularTypeface: Typeface
    ) {
        val labelColWidth = if (totalWidth > 500f) 100f else 85f
        val halfWidth = totalWidth / 2f
        val halfLabelWidth = if (totalWidth > 500f) 85f else 65f

        when (row) {
            is RenderRow.MultiLine -> {
                drawGridRow2ColsMultiLine(
                    canvas, textPaint, x, y, totalWidth, labelColWidth, height,
                    row.entry.label, row.entry.value, borderColor, labelColor, valueColor,
                    boldTypeface, regularTypeface
                )
            }
            is RenderRow.Pair -> {
                drawGridRow4Cols(
                    canvas, x, y, totalWidth, halfWidth, halfLabelWidth, halfLabelWidth, height,
                    row.entry1.label, row.entry1.value, row.entry2.label, row.entry2.value,
                    borderColor, labelColor, valueColor, boldTypeface, regularTypeface
                )
            }
            is RenderRow.Single -> {
                drawGridRow2Cols(
                    canvas, x, y, totalWidth, labelColWidth, height,
                    row.entry.label, row.entry.value, borderColor, labelColor, valueColor,
                    boldTypeface, regularTypeface
                )
            }
        }
    }

    private fun drawGridRow2Cols(
        canvas: Canvas,
        x: Float,
        y: Float,
        totalWidth: Float,
        labelColWidth: Float,
        height: Float,
        label: String,
        value: String,
        borderColor: Int,
        labelColor: Int,
        valueColor: Int,
        boldTypeface: Typeface,
        regularTypeface: Typeface
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(x, y, x + totalWidth, y + height, paint)
        canvas.drawLine(x + labelColWidth, y, x + labelColWidth, y + height, paint)

        paint.style = Paint.Style.FILL
        paint.color = labelColor
        paint.typeface = boldTypeface
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.LEFT
        val textY = y + (height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(label, x + 6f, textY, paint)

        paint.color = valueColor
        paint.typeface = regularTypeface
        val maxValWidth = (totalWidth - labelColWidth - 12f).coerceAtLeast(10f)
        val dispVal = if (paint.measureText(value) > maxValWidth) {
            val charCount = paint.breakText(value, true, maxValWidth - 8f, null)
            value.take(charCount) + "..."
        } else value
        canvas.drawText(dispVal, x + labelColWidth + 6f, textY, paint)
    }

    private fun drawGridRow4Cols(
        canvas: Canvas,
        x: Float,
        y: Float,
        totalWidth: Float,
        halfWidth: Float,
        label1Width: Float,
        label2Width: Float,
        height: Float,
        label1: String,
        value1: String,
        label2: String,
        value2: String,
        borderColor: Int,
        labelColor: Int,
        valueColor: Int,
        boldTypeface: Typeface,
        regularTypeface: Typeface
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(x, y, x + totalWidth, y + height, paint)
        canvas.drawLine(x + halfWidth, y, x + halfWidth, y + height, paint)
        canvas.drawLine(x + label1Width, y, x + label1Width, y + height, paint)
        canvas.drawLine(x + halfWidth + label2Width, y, x + halfWidth + label2Width, y + height, paint)

        paint.style = Paint.Style.FILL
        paint.color = labelColor
        paint.typeface = boldTypeface
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.LEFT
        val textY = y + (height / 2f) - ((paint.descent() + paint.ascent()) / 2f)

        canvas.drawText(label1, x + 5f, textY, paint)

        paint.color = valueColor
        paint.typeface = regularTypeface
        val maxVal1Width = (halfWidth - label1Width - 10f).coerceAtLeast(10f)
        val dispVal1 = if (paint.measureText(value1) > maxVal1Width) {
            val charCount = paint.breakText(value1, true, maxVal1Width - 8f, null)
            value1.take(charCount) + "..."
        } else value1
        canvas.drawText(dispVal1, x + label1Width + 5f, textY, paint)

        paint.color = labelColor
        paint.typeface = boldTypeface
        canvas.drawText(label2, x + halfWidth + 5f, textY, paint)

        paint.color = valueColor
        paint.typeface = regularTypeface
        val maxVal2Width = (totalWidth - halfWidth - label2Width - 10f).coerceAtLeast(10f)
        val dispVal2 = if (paint.measureText(value2) > maxVal2Width) {
            val charCount = paint.breakText(value2, true, maxVal2Width - 8f, null)
            value2.take(charCount) + "..."
        } else value2
        canvas.drawText(dispVal2, x + halfWidth + label2Width + 5f, textY, paint)
    }

    private fun drawGridRow2ColsMultiLine(
        canvas: Canvas,
        textPaint: TextPaint,
        x: Float,
        y: Float,
        totalWidth: Float,
        labelColWidth: Float,
        height: Float,
        label: String,
        value: String,
        borderColor: Int,
        labelColor: Int,
        valueColor: Int,
        boldTypeface: Typeface,
        regularTypeface: Typeface
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(x, y, x + totalWidth, y + height, paint)
        canvas.drawLine(x + labelColWidth, y, x + labelColWidth, y + height, paint)

        paint.style = Paint.Style.FILL
        paint.color = labelColor
        paint.typeface = boldTypeface
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(label, x + 6f, y + 16f, paint)

        textPaint.color = valueColor
        textPaint.typeface = regularTypeface
        textPaint.textSize = 8.5f

        val valWidth = (totalWidth - labelColWidth - 12f).toInt().coerceAtLeast(10)
        canvas.save()
        canvas.translate(x + labelColWidth + 6f, y + 6f)
        val staticLayout = StaticLayout.Builder.obtain(value, 0, value.length, textPaint, valWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f, 1f)
            .setIncludePad(false)
            .build()
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun calculateRowHeight(
        text: String,
        availableWidth: Float,
        lineHeight: Float,
        minHeight: Float,
        typeface: Typeface
    ): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f
            this.typeface = typeface
        }
        val lines = text.split("\n")
        var totalLines = 0
        for (line in lines) {
            val textWidth = paint.measureText(line)
            val wraps = Math.ceil((textWidth / (availableWidth - 12f)).toDouble()).toInt().coerceAtLeast(1)
            totalLines += wraps
        }
        val calculated = (totalLines * 12f) + 12f
        return Math.max(calculated, minHeight)
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
        canvas.translate(width * 0.50f, height * 0.53f)
        canvas.rotate(-24f)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = 190f
            this.typeface = typeface
            textAlign = Paint.Align.CENTER
            style = Paint.Style.FILL
            clearShadowLayer()
        }
        canvas.drawText(text, 0f, 60f, paint)

        canvas.restore()
    }

    private fun loadBitmap(context: Context, uriString: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            var input: InputStream? = if (uri.scheme == "file" || uri.scheme == null) {
                FileInputStream(File(uri.path ?: uriString))
            } else {
                context.contentResolver.openInputStream(uri)
            }

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(input, null, options)
            input?.close()

            var inSampleSize = 1
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            input = if (uri.scheme == "file" || uri.scheme == null) {
                FileInputStream(File(uri.path ?: uriString))
            } else {
                context.contentResolver.openInputStream(uri)
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val result = BitmapFactory.decodeStream(input, null, decodeOptions)
            input?.close()
            result
        } catch (_: Exception) {
            null
        }
    }

    private fun drawCenterCropBitmap(canvas: Canvas, bitmap: Bitmap, dstRect: RectF) {
        val srcWidth = bitmap.width.toFloat()
        val srcHeight = bitmap.height.toFloat()
        val dstWidth = dstRect.width()
        val dstHeight = dstRect.height()

        val scale = Math.max(dstWidth / srcWidth, dstHeight / srcHeight)
        val scaledWidth = srcWidth * scale
        val scaledHeight = srcHeight * scale

        val srcLeft = ((scaledWidth - dstWidth) / 2f) / scale
        val srcTop = ((scaledHeight - dstHeight) / 2f) / scale
        val srcRight = srcLeft + (dstWidth / scale)
        val srcBottom = srcTop + (dstHeight / scale)

        val src = Rect(srcLeft.toInt(), srcTop.toInt(), srcRight.toInt(), srcBottom.toInt())
        canvas.drawBitmap(bitmap, src, dstRect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
    }

    suspend fun savePdfToDownloads(context: Context, pdfFile: File, displayName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(pdfFile).use { input -> input.copyTo(out) }
                    }
                    true
                } else false
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetFile = File(downloadsDir, displayName)
                FileInputStream(pdfFile).use { input ->
                    FileOutputStream(targetFile).use { output -> input.copyTo(output) }
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
