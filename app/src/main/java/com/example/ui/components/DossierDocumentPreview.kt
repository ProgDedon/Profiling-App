package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DossierProfile
import java.io.File

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
            .fillMaxWidth(),
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Subtle Watermark in Background
            if (profile.watermarkText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.watermarkText,
                        color = Color(0x0E000000),
                        fontSize = 110.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier.rotate(-28f)
                    )
                }
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
                        text = profile.fullName.ifBlank { "FULL NAME" }.uppercase(),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
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
                                fontFamily = FontFamily.Serif,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }

                        // Phone number under photo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .border(0.5.dp, borderColor)
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.displayPhotoNumber.ifBlank { profile.phoneNumber.ifBlank { "—" } },
                                color = Color(0xFF1E293B),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }

                    // --- RIGHT TABLES COLUMN ---
                    Column(
                        modifier = Modifier.weight(0.68f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // SECTION 1: PERSONAL & CONTACT INFORMATION
                        SectionTable(
                            headerTitle = "PERSONAL & CONTACT INFORMATION",
                            headerColor = headerColor,
                            borderColor = borderColor
                        ) {
                            // Row 1: Gender & LGA/State
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                            ) {
                                Cell(text = "Gender:", isLabel = true, weight = 0.22f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                                Cell(text = profile.gender.ifBlank { "—" }, isLabel = false, weight = 0.28f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                                Cell(text = "LGA/State:", isLabel = true, weight = 0.24f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                                Cell(text = profile.lgaState.ifBlank { "—" }, isLabel = false, weight = 0.26f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                            }
                            // Row 2: Phone Number & Also Known As
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                            ) {
                                Cell(text = "Phone Number:", isLabel = true, weight = 0.22f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                                Cell(text = profile.phoneNumber.ifBlank { "—" }, isLabel = false, weight = 0.28f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                                Cell(text = "Also Known As", isLabel = true, weight = 0.24f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                                Cell(text = profile.alsoKnownAs.ifBlank { "—" }, isLabel = false, weight = 0.26f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                            }
                            // Row 3: Date of Birth
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                            ) {
                                Cell(text = "Date of Birth", isLabel = true, weight = 0.22f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                                Cell(text = profile.dateOfBirth.ifBlank { "—" }, isLabel = false, weight = 0.78f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                            }
                        }

                        // SECTION 2: SOCIAL MEDIA
                        SectionTable(
                            headerTitle = "SOCIAL MEDIA",
                            headerColor = headerColor,
                            borderColor = borderColor
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                            ) {
                                Cell(text = "Facebook", isLabel = true, weight = 0.22f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                                Cell(text = profile.facebook.ifBlank { "—" }, isLabel = false, weight = 0.78f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                            }
                            if (profile.otherSocialMedia.isNotBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min)
                                ) {
                                    Cell(text = "Other", isLabel = true, weight = 0.22f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                                    Cell(text = profile.otherSocialMedia, isLabel = false, weight = 0.78f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                                }
                            }
                        }

                        // SECTION 3: OTHER INFORMATION
                        SectionTable(
                            headerTitle = "OTHER INFORMATION",
                            headerColor = headerColor,
                            borderColor = borderColor
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                            ) {
                                Cell(text = "Occupation", isLabel = true, weight = 0.22f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                                Cell(text = profile.occupation.ifBlank { "—" }, isLabel = false, weight = 0.78f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                            ) {
                                Cell(text = "Education", isLabel = true, weight = 0.22f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                                Cell(text = profile.education.ifBlank { "—" }, isLabel = false, weight = 0.78f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                            ) {
                                Cell(text = "Associates Phone\nNumbers", isLabel = true, weight = 0.22f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
                                Cell(text = profile.associatesPhoneNumbers.ifBlank { "—" }, isLabel = false, weight = 0.78f, borderColor = borderColor, labelColor = labelColor, valueColor = valueColor)
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
                fontFamily = FontFamily.Serif
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
            .padding(horizontal = 4.dp, vertical = 3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = if (isLabel) labelColor else valueColor,
            fontWeight = if (isLabel) FontWeight.Bold else FontWeight.Normal,
            fontSize = 8.5.sp,
            fontFamily = FontFamily.Serif,
            lineHeight = 11.sp
        )
    }
}
