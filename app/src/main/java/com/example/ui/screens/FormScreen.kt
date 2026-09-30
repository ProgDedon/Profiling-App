package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
    onRequestCameraForPhoto: () -> Unit,
    onGenderChange: (String) -> Unit,
    onLgaStateChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onAlsoKnownAsChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onFacebookChange: (String) -> Unit,
    onTwitterChange: (String) -> Unit,
    onInstagramChange: (String) -> Unit,
    onYoutubeChange: (String) -> Unit,
    onOtherSocialChange: (String) -> Unit,
    onOccupationChange: (String) -> Unit,
    onEducationChange: (String) -> Unit,
    onAssociatesChange: (String) -> Unit,
    onAddPersonalInfoField: (String, String) -> Unit,
    onUpdatePersonalInfoField: (String, String, String) -> Unit,
    onRemovePersonalInfoField: (String) -> Unit,
    onAddOtherInfoField: (String, String) -> Unit,
    onUpdateOtherInfoField: (String, String, String) -> Unit,
    onRemoveOtherInfoField: (String) -> Unit,
    onWatermarkChange: (String) -> Unit,
    onHeaderColorChange: (String) -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onNavigateToPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Universal Android Gallery / Image Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onPhotoSelected(uri)
        }
    }

    val scrollState = rememberScrollState()
    val personalFields = profile.getPersonalInfoList()
    val otherFields = profile.getOtherInfoList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Action Bar: Reset, Save
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onReset,
                        modifier = Modifier.testTag("reset_form_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("save_profile_button")
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("", fontSize = 12.sp)
                    }
                }
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
                    text = "Profile Photo & Identity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp, 125.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE2E8F0))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                            .clickable {
                                photoPickerLauncher.launch("image/*")
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

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Upload button from gallery
                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pick_photo_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload from Gallery", fontSize = 12.sp)
                        }

                        // Camera capture button for portrait
                        OutlinedButton(
                            onClick = onRequestCameraForPhoto,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("camera_photo_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Take Photo", fontSize = 12.sp)
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
                                Text("Remove Photo", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Full Name
                OutlinedTextField(
                    value = profile.fullName,
                    onValueChange = onFullNameChange,
                    label = { Text("Full Name") },
                    placeholder = { Text("Enter full name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("full_name_input"),
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

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Gender:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Male", "Female", "").forEach { g ->
                            val label = if (g.isEmpty()) "Omit" else g
                            val isSelected = profile.gender == g
                            FilterChip(
                                selected = isSelected,
                                onClick = { onGenderChange(g) },
                                label = { Text(label) },
                                modifier = Modifier.testTag("gender_chip_$label")
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = profile.lgaState,
                    onValueChange = onLgaStateChange,
                    label = { Text("LGA / State") },
                    placeholder = { Text("e.g. Kontagora, Niger State") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lga_state_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = profile.phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    label = { Text("Phone Number(s)") },
                    placeholder = { Text("e.g. 07062270031, 08033334444") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_number_input"),
                    singleLine = false,
                    maxLines = 3
                )

                OutlinedTextField(
                    value = profile.alsoKnownAs,
                    onValueChange = onAlsoKnownAsChange,
                    label = { Text("Also Known As (Alias)") },
                    placeholder = { Text("e.g. Alias or Nickname") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("also_known_as_input"),
                    singleLine = true
                )

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

                // Additional items in Personal & Contact Information
                if (personalFields.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        personalFields.forEachIndexed { index, field ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("personal_field_row_$index"),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = field.label,
                                    onValueChange = { newLabel ->
                                        onUpdatePersonalInfoField(field.id, newLabel, field.value)
                                    },
                                    label = { Text("Field Label") },
                                    placeholder = { Text("e.g. State of Origin") },
                                    modifier = Modifier.weight(0.42f),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = field.value,
                                    onValueChange = { newVal ->
                                        onUpdatePersonalInfoField(field.id, field.label, newVal)
                                    },
                                    label = { Text("Value") },
                                    placeholder = { Text("Value") },
                                    modifier = Modifier.weight(0.58f),
                                    singleLine = true
                                )

                                IconButton(
                                    onClick = { onRemovePersonalInfoField(field.id) }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove Field",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                // Add More Information Button for Personal & Contact
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onAddPersonalInfoField("", "") },
                        modifier = Modifier.testTag("add_personal_info_field_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add More Information", fontSize = 12.sp)
                    }
                }
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
                    text = "Social Media Profiles",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = profile.facebook,
                    onValueChange = onFacebookChange,
                    label = { Text("Facebook") },
                    placeholder = { Text("Username or profile link") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("facebook_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = profile.twitter,
                    onValueChange = onTwitterChange,
                    label = { Text("Twitter / X") },
                    placeholder = { Text("@handle") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("twitter_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = profile.instagram,
                    onValueChange = onInstagramChange,
                    label = { Text("Instagram") },
                    placeholder = { Text("@handle") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("instagram_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = profile.youtube,
                    onValueChange = onYoutubeChange,
                    label = { Text("YouTube") },
                    placeholder = { Text("Channel handle or link") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("youtube_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = profile.otherSocialMedia,
                    onValueChange = onOtherSocialChange,
                    label = { Text("Other Social Media (Optional)") },
                    placeholder = { Text("e.g. LinkedIn, TikTok, Telegram") },
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
                    label = { Text("Occupation / Role") },
                    placeholder = { Text("e.g. Software Engineer / Consultant") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("occupation_input")
                )

                OutlinedTextField(
                    value = profile.education,
                    onValueChange = onEducationChange,
                    label = { Text("Education") },
                    placeholder = { Text("e.g. University / Institution") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("education_input")
                )

                OutlinedTextField(
                    value = profile.associatesPhoneNumbers,
                    onValueChange = onAssociatesChange,
                    label = { Text("Associates Phone Numbers") },
                    placeholder = { Text("e.g. 08100000000 (Contact Name)") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("associates_input")
                )

                // Additional items in Other Information
                if (otherFields.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        otherFields.forEachIndexed { index, field ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("other_field_row_$index"),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = field.label,
                                    onValueChange = { newLabel ->
                                        onUpdateOtherInfoField(field.id, newLabel, field.value)
                                    },
                                    label = { Text("Field Label") },
                                    placeholder = { Text("e.g. Party Affiliation") },
                                    modifier = Modifier.weight(0.42f),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = field.value,
                                    onValueChange = { newVal ->
                                        onUpdateOtherInfoField(field.id, field.label, newVal)
                                    },
                                    label = { Text("Value") },
                                    placeholder = { Text("Value") },
                                    modifier = Modifier.weight(0.58f),
                                    singleLine = true
                                )

                                IconButton(
                                    onClick = { onRemoveOtherInfoField(field.id) }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove Field",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                // Add More Information Button for Other Information
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onAddOtherInfoField("", "") },
                        modifier = Modifier.testTag("add_other_info_field_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add More Information", fontSize = 12.sp)
                    }
                }
            }
        }

        // SECTION: WATERMARK & COLOR
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

                OutlinedTextField(
                    value = "MMC",
                    onValueChange = { /* permanent watermark */ },
                    label = { Text("Background Watermark") },
                    supportingText = { Text("Permanent watermark: MMC") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("watermark_input"),
                    readOnly = true,
                    singleLine = true
                )

                Text(
                    text = "Header Color Scheme",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                val colorOptions = listOf(
                    "#102E56" to "Navy",
                    "#0F4C81" to "Blue",
                    "#1E3A8A" to "Royal",
                    "#0F172A" to "Slate",
                    "#14532D" to "Emerald",
                    "#7F1D1D" to "Crimson"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { (hex, _) ->
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

        Spacer(modifier = Modifier.height(16.dp))

        // Attribution
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Powered by Grey Communication @ 08101379193",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
