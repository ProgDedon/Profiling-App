package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DossierProfile
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    profile: DossierProfile,
    onFullNameChange: (String) -> Unit,
    onPhotoSelected: (Uri) -> Unit,
    onPhotoRemoved: () -> Unit,
    onPhotoDisplayNumberChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onLgaStateChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onAlsoKnownAsChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onFacebookChange: (String) -> Unit,
    onOtherSocialChange: (String) -> Unit,
    onOccupationChange: (String) -> Unit,
    onEducationChange: (String) -> Unit,
    onAssociatesChange: (String) -> Unit,
    onWatermarkChange: (String) -> Unit,
    onHeaderColorChange: (String) -> Unit,
    onLoadSample: () -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onNavigateToPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPhotoSelected(uri)
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Action Bar: Sample, Reset, Save
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Document Builder",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onLoadSample,
                            modifier = Modifier.testTag("load_sample_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sample", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onReset,
                            modifier = Modifier.testTag("reset_form_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onSave,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("save_profile_button")
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", fontSize = 12.sp)
                        }
                    }
                }
                Text(
                    text = "Fill in the dossier details below. All fields match the official template format.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // SECTION: PROFILE PHOTO & IDENTITY
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Photo & Identity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Photo Preview Box
                    Box(
                        modifier = Modifier
                            .size(100.dp, 125.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE2E8F0))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val photoModel = profile.photoUri?.let { path ->
                            if (path.startsWith("content://") || path.startsWith("file://")) path else File(path)
                        }

                        if (photoModel != null) {
                            AsyncImage(
                                model = photoModel,
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Upload Photo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Add Photo",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Photo Action Buttons & Guidance
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pick_photo_button")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (profile.photoUri != null) "Change Photo" else "Select Photo", fontSize = 13.sp)
                        }

                        if (profile.photoUri != null) {
                            OutlinedButton(
                                onClick = onPhotoRemoved,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("remove_photo_button")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Remove Photo", fontSize = 13.sp)
                            }
                        }
                        Text(
                            text = "Recommended: portrait photo, 3:4 or 4:5 aspect ratio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Full Name
                OutlinedTextField(
                    value = profile.fullName,
                    onValueChange = onFullNameChange,
                    label = { Text("Full Name (Header Banner & Under Photo)") },
                    placeholder = { Text("e.g. MUBARAK ADULLAHI") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("full_name_input"),
                    singleLine = true
                )

                // Photo Display Phone (defaults to primary phone)
                OutlinedTextField(
                    value = profile.photoDisplayNumber,
                    onValueChange = onPhotoDisplayNumberChange,
                    label = { Text("Phone Number Under Photo (Optional Override)") },
                    placeholder = { Text(profile.phoneNumber.ifBlank { "e.g. 07062270031" }) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("photo_display_number_input"),
                    singleLine = true
                )
            }
        }

        // SECTION 1: PERSONAL & CONTACT INFORMATION
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Personal & Contact Information",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Gender Chips
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Gender:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Male", "Female", "Other").forEach { g ->
                            FilterChip(
                                selected = profile.gender.equals(g, ignoreCase = true),
                                onClick = { onGenderChange(g) },
                                label = { Text(g) },
                                modifier = Modifier.testTag("gender_chip_$g")
                            )
                        }
                    }
                }

                // LGA / State
                OutlinedTextField(
                    value = profile.lgaState,
                    onValueChange = onLgaStateChange,
                    label = { Text("LGA/State") },
                    placeholder = { Text("e.g. Kontagori, Niger State") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lga_state_input"),
                    singleLine = true
                )

                // Primary Phone Number
                OutlinedTextField(
                    value = profile.phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    label = { Text("Primary Phone Number") },
                    placeholder = { Text("e.g. 07062270031") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_number_input"),
                    singleLine = true
                )

                // Also Known As (Alias)
                OutlinedTextField(
                    value = profile.alsoKnownAs,
                    onValueChange = onAlsoKnownAsChange,
                    label = { Text("Also Known As (Alias / Nickname)") },
                    placeholder = { Text("e.g. Sardaunan Samari") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("also_known_as_input"),
                    singleLine = true
                )

                // Date of Birth
                OutlinedTextField(
                    value = profile.dateOfBirth,
                    onValueChange = onDateOfBirthChange,
                    label = { Text("Date of Birth") },
                    placeholder = { Text("e.g. 18th August, 1992") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dob_input"),
                    singleLine = true
                )
            }
        }

        // SECTION 2: SOCIAL MEDIA
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Social Media",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = profile.facebook,
                    onValueChange = onFacebookChange,
                    label = { Text("Facebook Username / Handle") },
                    placeholder = { Text("e.g. Mubarak.abdullahi.739") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("facebook_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = profile.otherSocialMedia,
                    onValueChange = onOtherSocialChange,
                    label = { Text("Other Social Media (Optional)") },
                    placeholder = { Text("e.g. Twitter / X: @mubarak_ad") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("other_social_input"),
                    singleLine = true
                )
            }
        }

        // SECTION 3: OTHER INFORMATION
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Other Information",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = profile.occupation,
                    onValueChange = onOccupationChange,
                    label = { Text("Occupation / Roles") },
                    placeholder = { Text("e.g. Activist/ Advocate / ADC Supporter") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("occupation_input")
                )

                OutlinedTextField(
                    value = profile.education,
                    onValueChange = onEducationChange,
                    label = { Text("Education") },
                    placeholder = { Text("e.g. Government Secondary School Kontagora, Niger State") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("education_input")
                )

                OutlinedTextField(
                    value = profile.associatesPhoneNumbers,
                    onValueChange = onAssociatesChange,
                    label = { Text("Associates Phone Numbers") },
                    placeholder = { Text("08109595044 (Yusuf ALIYU)\n07037882149 (Musa Reskona ABUBAKAR)") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("associates_input")
                )
            }
        }

        // SECTION: WATERMARK & TEMPLATE COLOR CUSTOMIZATION
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Document Styling & Watermark",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Watermark Text
                OutlinedTextField(
                    value = profile.watermarkText,
                    onValueChange = onWatermarkChange,
                    label = { Text("Background Watermark Text (Optional)") },
                    placeholder = { Text("e.g. NS, CONFIDENTIAL, OFFICIAL") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("watermark_input"),
                    singleLine = true
                )

                // Header Color Selection
                Text(
                    text = "Header Banner Color Scheme",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                val colorOptions = listOf(
                    "#102E56" to "Template Navy",
                    "#0F4C81" to "Classic Blue",
                    "#1E3A8A" to "Royal Navy",
                    "#0F172A" to "Slate Dark",
                    "#14532D" to "Deep Emerald",
                    "#7F1D1D" to "Crimson Red"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { (hex, name) ->
                        val isSelected = profile.headerColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable { onHeaderColorChange(hex) }
                        )
                    }
                }
            }
        }

        // Preview & Generate PDF Button
        Button(
            onClick = onNavigateToPreview,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("preview_and_generate_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Visibility, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Preview & Generate PDF", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
