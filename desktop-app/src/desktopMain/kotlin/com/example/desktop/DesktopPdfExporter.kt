package com.example.desktop

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import java.io.File

object DesktopPdfExporter {

    fun exportA4Landscape(profile: DesktopProfile, outputFile: File) {
        PDDocument().use { document ->
            // A4 Landscape: width 842 pt, height 595 pt
            val pageRect = PDRectangle(841.89f, 595.28f)
            val page = PDPage(pageRect)
            document.addPage(page)

            PDPageContentStream(document, page).use { cs ->
                val boldFont = PDType1Font(Standard14Fonts.FontName.TIMES_BOLD)
                val regularFont = PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN)

                val pageWidth = pageRect.width
                val pageHeight = pageRect.height
                val margin = 36f

                // Draw Header Banner
                cs.setNonStrokingColor(0.12f, 0.16f, 0.23f) // BrandNavy
                cs.addRect(margin, pageHeight - 70f, pageWidth - (margin * 2), 40f)
                cs.fill()

                // Header Title
                cs.beginText()
                cs.setFont(boldFont, 16f)
                cs.setNonStrokingColor(0.77f, 0.63f, 0.35f) // BrandGold
                cs.newLineAtOffset(margin + 16f, pageHeight - 54f)
                cs.showText("greymmc • EXECUTIVE DOSSIER REPORT (A4 LANDSCAPE)")
                cs.endText()

                // Confidential Tag
                cs.beginText()
                cs.setFont(boldFont, 10f)
                cs.setNonStrokingColor(1f, 1f, 1f)
                cs.newLineAtOffset(pageWidth - margin - 110f, pageHeight - 54f)
                cs.showText("CONFIDENTIAL")
                cs.endText()

                // Subject Name Subtitle
                var cursorY = pageHeight - 90f
                cs.beginText()
                cs.setFont(boldFont, 14f)
                cs.setNonStrokingColor(0.12f, 0.16f, 0.23f)
                cs.newLineAtOffset(margin, cursorY)
                cs.showText(profile.fullName.ifBlank { "SUBJECT FULL NAME" })
                cs.endText()

                cursorY -= 20f

                // Section 1: Personal & Contact Information
                cursorY = drawSectionHeader(cs, boldFont, "1. PERSONAL & CONTACT INFORMATION", margin, cursorY, pageWidth)
                cursorY = drawRowPair(cs, regularFont, boldFont, "Full Name", profile.fullName, "Address", profile.address, margin, cursorY, pageWidth)
                cursorY = drawRowPair(cs, regularFont, boldFont, "Gender", profile.gender, "LGA & State", profile.lgaState, margin, cursorY, pageWidth)
                cursorY = drawRowPair(cs, regularFont, boldFont, "Phone Number(s)", profile.phoneNumber, "Date of Birth", profile.dateOfBirth, margin, cursorY, pageWidth)
                cursorY = drawRowSingle(cs, regularFont, boldFont, "Also Known As (AKA)", profile.alsoKnownAs, margin, cursorY, pageWidth)

                cursorY -= 10f

                // Section 2: Social Media
                cursorY = drawSectionHeader(cs, boldFont, "2. SOCIAL MEDIA", margin, cursorY, pageWidth)
                cursorY = drawRowPair(cs, regularFont, boldFont, "Facebook", profile.facebook, "Twitter / X", profile.twitter, margin, cursorY, pageWidth)
                cursorY = drawRowPair(cs, regularFont, boldFont, "Instagram", profile.instagram, "TikTok", profile.tiktok, margin, cursorY, pageWidth)
                cursorY = drawRowPair(cs, regularFont, boldFont, "YouTube", profile.youtube, "Email Address(es)", profile.email, margin, cursorY, pageWidth)
                cursorY = drawRowPair(cs, regularFont, boldFont, "Websites", profile.websites, "Other Social Media", profile.otherSocialMedia, margin, cursorY, pageWidth)

                cursorY -= 10f

                // Section 3: Other Information
                cursorY = drawSectionHeader(cs, boldFont, "3. OTHER INFORMATION", margin, cursorY, pageWidth)
                cursorY = drawRowSingle(cs, regularFont, boldFont, "Occupation/ Role", profile.occupation, margin, cursorY, pageWidth)
                cursorY = drawRowSingle(cs, regularFont, boldFont, "Education / Qualifications", profile.education, margin, cursorY, pageWidth)
                cursorY = drawRowSingle(cs, regularFont, boldFont, "Associates Phone Numbers", profile.associatesPhoneNumbers, margin, cursorY, pageWidth)

                // Footer
                cs.setStrokingColor(0.77f, 0.63f, 0.35f)
                cs.setLineWidth(1f)
                cs.moveTo(margin, 36f)
                cs.lineTo(pageWidth - margin, 36f)
                cs.stroke()

                cs.beginText()
                cs.setFont(regularFont, 8f)
                cs.setNonStrokingColor(0.4f, 0.4f, 0.4f)
                cs.newLineAtOffset(margin, 24f)
                cs.showText("Generated by greymmc Executive Desktop Workstation • A4 Landscape 842 x 595 pt • Page 1 of 1")
                cs.endText()
            }

            document.save(outputFile)
        }
    }

    private fun drawSectionHeader(cs: PDPageContentStream, font: PDType1Font, title: String, margin: Float, y: Float, pageWidth: Float): Float {
        cs.setNonStrokingColor(0.12f, 0.16f, 0.23f)
        cs.addRect(margin, y - 16f, pageWidth - (margin * 2), 18f)
        cs.fill()

        cs.beginText()
        cs.setFont(font, 10f)
        cs.setNonStrokingColor(0.77f, 0.63f, 0.35f)
        cs.newLineAtOffset(margin + 8f, y - 12f)
        cs.showText(title)
        cs.endText()

        return y - 22f
    }

    private fun drawRowPair(cs: PDPageContentStream, regularFont: PDType1Font, boldFont: PDType1Font, l1: String, v1: String, l2: String, v2: String, margin: Float, y: Float, pageWidth: Float): Float {
        val colWidth = (pageWidth - (margin * 2)) / 2f

        cs.beginText()
        cs.setFont(boldFont, 8.5f)
        cs.setNonStrokingColor(0.3f, 0.3f, 0.3f)
        cs.newLineAtOffset(margin + 6f, y - 8f)
        cs.showText("$l1: ")
        cs.setFont(regularFont, 9.5f)
        cs.setNonStrokingColor(0f, 0f, 0f)
        cs.showText(v1.ifBlank { "—" })
        cs.endText()

        cs.beginText()
        cs.setFont(boldFont, 8.5f)
        cs.setNonStrokingColor(0.3f, 0.3f, 0.3f)
        cs.newLineAtOffset(margin + colWidth + 6f, y - 8f)
        cs.showText("$l2: ")
        cs.setFont(regularFont, 9.5f)
        cs.setNonStrokingColor(0f, 0f, 0f)
        cs.showText(v2.ifBlank { "—" })
        cs.endText()

        // Border line
        cs.setStrokingColor(0.9f, 0.9f, 0.9f)
        cs.setLineWidth(0.5f)
        cs.moveTo(margin, y - 12f)
        cs.lineTo(pageWidth - margin, y - 12f)
        cs.stroke()

        return y - 16f
    }

    private fun drawRowSingle(cs: PDPageContentStream, regularFont: PDType1Font, boldFont: PDType1Font, label: String, value: String, margin: Float, y: Float, pageWidth: Float): Float {
        cs.beginText()
        cs.setFont(boldFont, 8.5f)
        cs.setNonStrokingColor(0.3f, 0.3f, 0.3f)
        cs.newLineAtOffset(margin + 6f, y - 8f)
        cs.showText("$label: ")
        cs.setFont(regularFont, 9.5f)
        cs.setNonStrokingColor(0f, 0f, 0f)
        cs.showText(value.ifBlank { "—" })
        cs.endText()

        cs.setStrokingColor(0.9f, 0.9f, 0.9f)
        cs.setLineWidth(0.5f)
        cs.moveTo(margin, y - 12f)
        cs.lineTo(pageWidth - margin, y - 12f)
        cs.stroke()

        return y - 16f
    }
}
