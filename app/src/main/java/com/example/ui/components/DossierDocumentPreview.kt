package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.DossierProfile
import java.io.File

val BookmanFontFamily = FontFamily(
    Font(R.font.bookman_old_style, FontWeight.Normal),
    Font(R.font.bookman_old_style, FontWeight.Bold)
)

@Composable
fun DossierDocumentPreview(
    profile: DossierProfile,
    modifier: Modifier = Modifier
) {
    val headerColor = try {
        Color(android.graphics.Color.parseColor(profile.headerColorHex))
    } catch (_: Exception) {
        Color(0xFF102E56)
    }

    val borderColor = Color(0xFFCBD5E1)
    val labelColor = Color(0xFF0F172A)
    val valueColor = Color(0xFF1E293B)

    Card(
        modifier = modifier
            .testTag("dossier_document_card")
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(2.dp)),
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Permanent Watermark in Background: Clean "MMC" with no shadow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                val watermarkText = if (profile.watermarkText.isNotBlank() && profile.watermarkText != "by MMC") {
                    profile.watermarkText
                } else {
                    "MMC"
                }
                Text(
                    text = watermarkText,
                    color = Color(0x0C000000),
                    fontSize = 110.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontFamily = BookmanFontFamily,
                    modifier = Modifier.rotate(-28f)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Top Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerColor)
                        .padding(vertical = 10.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.fullName.ifBlank { "PROFILE DOSSIER" }.uppercase(),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BookmanFontFamily,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Main Content: Left Column (Photo Card) & Right Column (Tables)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // --- LEFT PHOTO CARD ---
                    Column(
                        modifier = Modifier
                            .weight(0.32f)
                            .background(Color.White)
                            .border(1.dp, borderColor)
                    ) {
                        // Photo Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .aspectRatio(0.82f)
                                .background(Color(0xFFF1F5F9))
                                .border(0.5.dp, Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            val photoModel = profile.photoUri?.let { path ->
                                if (path.startsWith("content://") || path.startsWith("file://")) path else File(path)
                            }

                            if (photoModel != null) {
                                AsyncImage(
                                    model = photoModel,
                                    contentDescription = "Profile Photo of ${profile.fullName}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Default Photo",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    val initials = profile.fullName.split(" ")
                                        .filter { it.isNotBlank() }
                                        .take(2)
                                        .joinToString("") { it.take(1).uppercase() }
                                        .ifEmpty { "?" }
                                    Text(
                                        text = initials,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = BookmanFontFamily,
                                        color = headerColor
                                    )
                                }
                            }
                        }

                        // Name bar under photo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(headerColor)
                                .padding(vertical = 5.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.fullName.ifBlank { "FULL NAME" }.uppercase(),
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = BookmanFontFamily,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }

                        // Phone number under photo (ONLY IF NOT BLANK!)
                        if (profile.displayPhotoNumber.isNotBlank()) {
                            val phoneList = profile.displayPhotoNumber.split(Regex("[\n,/]+")).map { it.trim() }.filter { it.isNotBlank() }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .border(0.5.dp, borderColor)
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    phoneList.take(3).forEach { num ->
                                        Text(
                                            text = num,
                                            color = Color(0xFF1E293B),
                                            fontSize = if (phoneList.size > 1) 8.sp else 9.sp,
                                            fontFamily = BookmanFontFamily,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- RIGHT TABLES COLUMN ---
                    Column(
                        modifier = Modifier.weight(0.68f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // SECTION 1: PERSONAL & CONTACT INFORMATION
                        val personalEntries = remember(profile) {
                            val list = mutableListOf<PreviewFieldEntry>()
                            if (profile.gender.isNotBlank()) list.add(PreviewFieldEntry("Gender", profile.gender))
                            if (profile.lgaState.isNotBlank()) list.add(PreviewFieldEntry("LGA/State", profile.lgaState))
                            if (profile.phoneNumber.isNotBlank()) {
                                val isMulti = profile.phoneNumber.contains("\n") || profile.phoneNumber.length > 25
                                list.add(PreviewFieldEntry("Phone Number(s)", profile.phoneNumber, isMulti))
                            }
                            if (profile.alsoKnownAs.isNotBlank()) list.add(PreviewFieldEntry("Also Known As", profile.alsoKnownAs))
                            if (profile.dateOfBirth.isNotBlank()) list.add(PreviewFieldEntry("Date of Birth", profile.dateOfBirth))

                            for (pf in profile.getPersonalInfoList()) {
                                if (pf.label.isNotBlank() && pf.value.isNotBlank()) {
                                    val isMulti = pf.value.contains("\n") || pf.value.length > 40
                                    list.add(PreviewFieldEntry(pf.label, pf.value, isMulti))
                                }
                            }
                            list
                        }

                        if (personalEntries.isNotEmpty()) {
                            SectionTable(
                                headerTitle = "PERSONAL & CONTACT INFORMATION",
                                headerColor = headerColor,
                                borderColor = borderColor
                            ) {
                                SectionFieldsGrid(
                                    entries = personalEntries,
                                    borderColor = borderColor,
                                    labelColor = labelColor,
                                    valueColor = valueColor
                                )
                            }
                        }

                        // SECTION 2: SOCIAL MEDIA
                        val socialEntries = remember(profile) {
                            val list = mutableListOf<PreviewFieldEntry>()
                            if (profile.facebook.isNotBlank()) list.add(PreviewFieldEntry("Facebook", profile.facebook))
                            if (profile.twitter.isNotBlank()) list.add(PreviewFieldEntry("Twitter / X", profile.twitter))
                            if (profile.instagram.isNotBlank()) list.add(PreviewFieldEntry("Instagram", profile.instagram))
                            if (profile.youtube.isNotBlank()) list.add(PreviewFieldEntry("YouTube", profile.youtube))
                            if (profile.otherSocialMedia.isNotBlank()) list.add(PreviewFieldEntry("Other Social", profile.otherSocialMedia))
                            list
                        }

                        if (socialEntries.isNotEmpty()) {
                            SectionTable(
                                headerTitle = "SOCIAL MEDIA",
                                headerColor = headerColor,
                                borderColor = borderColor
                            ) {
                                SectionFieldsGrid(
                                    entries = socialEntries,
                                    borderColor = borderColor,
                                    labelColor = labelColor,
                                    valueColor = valueColor
                                )
                            }
                        }

                        // SECTION 3: OTHER INFORMATION & CUSTOM FIELDS
                        val otherEntries = remember(profile) {
                            val list = mutableListOf<PreviewFieldEntry>()
                            if (profile.occupation.isNotBlank()) list.add(PreviewFieldEntry("Occupation", profile.occupation))
                            if (profile.education.isNotBlank()) list.add(PreviewFieldEntry("Education", profile.education))
                            if (profile.associatesPhoneNumbers.isNotBlank()) {
                                val isMulti = profile.associatesPhoneNumbers.contains("\n") || profile.associatesPhoneNumbers.length > 32
                                list.add(PreviewFieldEntry("Associates Phone Numbers", profile.associatesPhoneNumbers, isMulti))
                            }

                            for (cf in profile.getOtherInfoList()) {
                                if (cf.label.isNotBlank() && cf.value.isNotBlank()) {
                                    val isMulti = cf.value.contains("\n") || cf.value.length > 40
                                    list.add(PreviewFieldEntry(cf.label, cf.value, isMulti))
                                }
                            }
                            list
                        }

                        if (otherEntries.isNotEmpty()) {
                            SectionTable(
                                headerTitle = "OTHER INFORMATION",
                                headerColor = headerColor,
                                borderColor = borderColor
                            ) {
                                SectionFieldsGrid(
                                    entries = otherEntries,
                                    borderColor = borderColor,
                                    labelColor = labelColor,
                                    valueColor = valueColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTable(
    headerTitle: String,
    headerColor: Color,
    borderColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor)
    ) {
        // Table Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerColor)
                .padding(vertical = 4.dp, horizontal = 6.dp)
        ) {
            Text(
                text = headerTitle,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = BookmanFontFamily
            )
        }
        content()
    }
}

@Composable
private fun RowScope.Cell(
    text: String,
    isLabel: Boolean,
    weight: Float,
    borderColor: Color,
    labelColor: Color,
    valueColor: Color
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .border(0.5.dp, borderColor)
            .background(Color.White)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = if (isLabel) labelColor else valueColor,
            fontWeight = if (isLabel) FontWeight.Bold else FontWeight.Normal,
            fontSize = 8.5.sp,
            fontFamily = BookmanFontFamily,
            lineHeight = 11.sp,
            softWrap = true
        )
    }
}

private data class PreviewFieldEntry(
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

@Composable
private fun SectionFieldsGrid(
    entries: List<PreviewFieldEntry>,
    borderColor: Color,
    labelColor: Color,
    valueColor: Color
) {
    var i = 0
    while (i < entries.size) {
        val current = entries[i]
        val next = if (i + 1 < entries.size) entries[i + 1] else null

        if (current.isSmall && next != null && next.isSmall) {
            // Two small fields in a row
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Cell(text = current.label, isLabel = true, weight = 0.16f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                Cell(text = current.value, isLabel = false, weight = 0.34f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                Cell(text = next.label, isLabel = true, weight = 0.16f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                Cell(text = next.value, isLabel = false, weight = 0.34f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
            }
            i += 2
        } else {
            // Single field in a row
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Cell(text = current.label, isLabel = true, weight = 0.20f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                Cell(text = current.value, isLabel = false, weight = 0.80f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
            }
            i += 1
        }
    }
}
