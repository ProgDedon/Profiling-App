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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DossierProfile

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
    onAddressChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onAlsoKnownAsChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onFacebookChange: (String) -> Unit,
    onTwitterChange: (String) -> Unit,
    onInstagramChange: (String) -> Unit,
    onTiktokChange: (String) -> Unit,
    onYoutubeChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onWebsitesChange: (String) -> Unit,
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
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
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
        // Top Action Bar: Reset & Save
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dossier Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.testTag("reset_form_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", fontSize = 12.sp)
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = "Save", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", fontSize = 12.sp)
                }
            }
        }

        // PROFILE PHOTO SECTION
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Text("No Photo", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier.testTag("pick_photo_button")
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gallery", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onRequestCameraForPhoto,
                        modifier = Modifier.testTag("camera_photo_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Camera", fontSize = 12.sp)
                    }

                    if (!profile.photoUri.isNullOrBlank()) {
                        IconButton(
                            onClick = onPhotoRemoved,
                            modifier = Modifier.testTag("remove_photo_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Photo", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
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

                OutlinedTextField(
                    value = profile.fullName,
                    onValueChange = onFullNameChange,
                    label = { Text("Full Name *") },
                    placeholder = { Text("e.g. Johnathan Doe") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("full_name_input"),
                    singleLine = true
                )

                // Gender Selection
                Column {
                    Text("Gender", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Male", "Female").forEach { genderOption ->
                            FilterChip(
                                selected = profile.gender == genderOption,
                                onClick = { onGenderChange(genderOption) },
                                label = { Text(genderOption) },
                                modifier = Modifier.testTag("gender_${genderOption.lowercase()}_chip")
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = profile.lgaState,
                    onValueChange = onLgaStateChange,
                    label = { Text("LGA / State") },
                    placeholder = { Text("e.g. Ikeja, Lagos") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lga_state_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = profile.address,
                    onValueChange = onAddressChange,
                    label = { Text("Address") },
                    placeholder = { Text("e.g. 12 Broad Street, Lagos") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("address_input")
                )

                OutlinedTextField(
                    value = profile.phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    label = { Text("Phone Number(s)") },
                    placeholder = { Text("08012345678, 09087654321") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_number_input")
                )

                OutlinedTextField(
                    value = profile.alsoKnownAs,
                    onValueChange = onAlsoKnownAsChange,
                    label = { Text("Also Known As (Alias)") },
                    placeholder = { Text("e.g. JD") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("also_known_as_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = profile.dateOfBirth,
                    onValueChange = onDateOfBirthChange,
                    label = { Text("Date of Birth") },
                    placeholder = { Text("e.g. 15th August 1985") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("date_of_birth_input"),
                    singleLine = true
                )

                // Custom Personal Fields
                val personalFields = profile.getPersonalInfoList()
                if (personalFields.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Additional Personal Fields", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
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

                                IconButton(onClick = { onRemovePersonalInfoField(field.id) }) {
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

        // SECTION 2: SOCIAL MEDIA PROFILES
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
                    value = profile.tiktok,
                    onValueChange = onTiktokChange,
                    label = { Text("TikTok") },
                    placeholder = { Text("@handle or link") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tiktok_input"),
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
                    value = profile.email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    placeholder = { Text("example@domain.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = profile.websites,
                    onValueChange = onWebsitesChange,
                    label = { Text("Websites") },
                    placeholder = { Text("https://example.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("websites_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = profile.otherSocialMedia,
                    onValueChange = onOtherSocialChange,
                    label = { Text("Other Social Media (Optional)") },
                    placeholder = { Text("e.g. LinkedIn, Telegram, Threads") },
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
                    label = { Text("Occupation/ Role") },
                    placeholder = { Text("e.g. Legal Practitioner, Engineer") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("occupation_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = profile.education,
                    onValueChange = onEducationChange,
                    label = { Text("Education") },
                    placeholder = { Text("e.g. B.Sc Political Science") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("education_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = profile.associatesPhoneNumbers,
                    onValueChange = onAssociatesChange,
                    label = { Text("Associates Phone Number(s)") },
                    placeholder = { Text("e.g. 08033333333 (Assistant)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("associates_input")
                )

                // Custom Other Fields
                val otherFields = profile.getOtherInfoList()
                if (otherFields.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Additional Fields", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
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
                                    placeholder = { Text("e.g. Office Address") },
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

                                IconButton(onClick = { onRemoveOtherInfoField(field.id) }) {
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

        // SECTION 4: STYLING & CUSTOMIZATION
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
                    text = "Header Color Theme",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                val colorOptions = listOf(
                    "#102E56" to "Navy",
                    "#1E3A8A" to "Blue",
                    "#047857" to "Emerald",
                    "#B91C1C" to "Crimson",
                    "#4338CA" to "Indigo",
                    "#111827" to "Charcoal"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { (hex, name) ->
                        val isSelected = profile.headerColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable { onHeaderColorChange(hex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
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

        // Attribution Footer
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
