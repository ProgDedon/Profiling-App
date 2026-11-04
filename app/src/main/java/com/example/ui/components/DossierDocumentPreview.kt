package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.DossierProfile

val BookmanFontFamily = try {
    FontFamily(
        Font(R.font.bookman_old_style, FontWeight.Normal),
        Font(R.font.bookman_old_style, FontWeight.Bold),
        Font(R.font.bookman_old_style, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.bookman_old_style, FontWeight.Bold, FontStyle.Italic)
    )
} catch (_: Exception) {
    FontFamily.Serif
}

data class PreviewFieldEntry(
    val label: String,
    val value: String,
    val isMultiLine: Boolean = false
)

@Composable
fun DossierDocumentPreview(
    profile: DossierProfile,
    modifier: Modifier = Modifier
) {
    val headerColor = remember(profile.headerColorHex) {
        try {
            Color(android.graphics.Color.parseColor(profile.headerColorHex))
        } catch (_: Exception) {
            Color(0xFF102E56)
        }
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
                    .padding(top = 100.dp),
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
                    modifier = Modifier.rotate(-24f),
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("document_content_column")
            ) {
                // Orientation & Standard Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.owl_logo_icon_1793603390711),
                            contentDescription = "greymmc Owl Emblem",
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                        Text(
                            text = "A4 LANDSCAPE  •  842 × 595 PT",
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            fontFamily = BookmanFontFamily,
                            fontSize = 8.5.sp
                        )
                    }
                    Text(
                        text = "OFFICIAL DOSSIER",
                        color = headerColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BookmanFontFamily,
                        fontSize = 8.5.sp
                    )
                }

                // Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerColor)
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.fullName.ifBlank { "DOSSIER PROFILE" }.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BookmanFontFamily,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Two column layout: Left (Photo + Phone) & Right (Tables)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left Column: Photo Box + Phone Numbers
                    Column(
                        modifier = Modifier.width(115.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Photo Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(145.dp)
                                .border(1.dp, borderColor, RoundedCornerShape(2.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profile.photoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = profile.photoUri,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.owl_logo_icon_1793603390711),
                                        contentDescription = "Owl Emblem",
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "OFFICIAL DOSSIER",
                                        color = Color(0xFF64748B),
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = BookmanFontFamily
                                    )
                                }
                            }
                        }

                        // Phone Number under photo
                        val rawPhone = profile.displayPhotoNumber
                        val phoneList = rawPhone.split("\n", ",", "/").map { it.trim() }.filter { it.isNotBlank() }
                        if (phoneList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderColor, RoundedCornerShape(2.dp))
                                    .background(Color.White)
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    phoneList.take(3).forEach { p ->
                                        Text(
                                            text = p,
                                            color = Color(0xFF111827),
                                            fontSize = if (phoneList.size == 1) 9.sp else 7.5.sp,
                                            fontWeight = FontWeight.Normal,
                                            fontFamily = BookmanFontFamily,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Right Column: Personal Info, Social Media, Other Info Tables
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // SECTION 1: PERSONAL & CONTACT INFORMATION
                        val personalEntries = remember(profile) {
                            val list = mutableListOf<PreviewFieldEntry>()
                            if (profile.gender.isNotBlank()) list.add(PreviewFieldEntry("Gender", profile.gender))
                            if (profile.lgaState.isNotBlank()) list.add(PreviewFieldEntry("LGA/State", profile.lgaState))
                            if (profile.address.isNotBlank()) {
                                val isMulti = profile.address.contains("\n") || profile.address.length > 25
                                list.add(PreviewFieldEntry("Address", profile.address, isMulti))
                            }
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
                            if (profile.tiktok.isNotBlank()) list.add(PreviewFieldEntry("TikTok", profile.tiktok))
                            if (profile.youtube.isNotBlank()) list.add(PreviewFieldEntry("YouTube", profile.youtube))
                            if (profile.email.isNotBlank()) {
                                val isMulti = profile.email.length > 28
                                list.add(PreviewFieldEntry("Email", profile.email, isMulti))
                            }
                            if (profile.websites.isNotBlank()) {
                                val isMulti = profile.websites.contains("\n") || profile.websites.length > 28
                                list.add(PreviewFieldEntry("Websites", profile.websites, isMulti))
                            }
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

                        // SECTION 3: OTHER INFORMATION
                        val otherEntries = remember(profile) {
                            val list = mutableListOf<PreviewFieldEntry>()
                            if (profile.occupation.isNotBlank()) list.add(PreviewFieldEntry("Occupation/ Role", profile.occupation))
                            if (profile.education.isNotBlank()) list.add(PreviewFieldEntry("Education", profile.education))
                            if (profile.associatesPhoneNumbers.isNotBlank()) {
                                val isMulti = profile.associatesPhoneNumbers.contains("\n") || profile.associatesPhoneNumbers.length > 30
                                list.add(PreviewFieldEntry("Associates Phone", profile.associatesPhoneNumbers, isMulti))
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

                Spacer(modifier = Modifier.height(14.dp))

                // Document Footer
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "greymmc  •  Page 1  •  A4 Landscape",
                        color = Color(0xFF94A3B8),
                        fontSize = 8.sp,
                        fontFamily = BookmanFontFamily
                    )
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
            .border(1.dp, borderColor, RoundedCornerShape(2.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerColor)
                .padding(vertical = 4.dp, horizontal = 6.dp)
        ) {
            Text(
                text = headerTitle,
                color = Color.White,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = BookmanFontFamily
            )
        }
        content()
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
        val entry1 = entries[i]
        if (entry1.isMultiLine) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, borderColor)
                    .padding(vertical = 3.dp, horizontal = 4.dp)
            ) {
                Text(
                    text = entry1.label,
                    color = labelColor,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = BookmanFontFamily,
                    modifier = Modifier.width(65.dp)
                )
                Text(
                    text = entry1.value,
                    color = valueColor,
                    fontSize = 7.5.sp,
                    fontFamily = BookmanFontFamily,
                    modifier = Modifier.weight(1f)
                )
            }
            i++
        } else if (i + 1 < entries.size && !entries[i + 1].isMultiLine) {
            val entry2 = entries[i + 1]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .border(0.5.dp, borderColor)
            ) {
                // Col 1
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 3.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry1.label,
                        color = labelColor,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BookmanFontFamily,
                        modifier = Modifier.width(52.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = entry1.value,
                        color = valueColor,
                        fontSize = 7.5.sp,
                        fontFamily = BookmanFontFamily,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(0.5.dp)
                        .background(borderColor)
                )

                // Col 2
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 3.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry2.label,
                        color = labelColor,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BookmanFontFamily,
                        modifier = Modifier.width(52.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = entry2.value,
                        color = valueColor,
                        fontSize = 7.5.sp,
                        fontFamily = BookmanFontFamily,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            i += 2
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, borderColor)
                    .padding(vertical = 3.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry1.label,
                    color = labelColor,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = BookmanFontFamily,
                    modifier = Modifier.width(65.dp)
                )
                Text(
                    text = entry1.value,
                    color = valueColor,
                    fontSize = 7.5.sp,
                    fontFamily = BookmanFontFamily,
                    modifier = Modifier.weight(1f)
                )
            }
            i++
        }
    }
}
