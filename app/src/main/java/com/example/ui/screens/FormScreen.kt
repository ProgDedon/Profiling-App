package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.material3.Surface
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
import com.example.data.model.CustomField
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
    onPhotoDisplayNumberChange: (String) -> Unit,
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
    onFormImageSelected: (Uri) -> Unit,
    onFormImageRemoved: () -> Unit,
    onRequestCameraForForm: () -> Unit,
    onAddCustomField: (String, String) -> Unit,
    onUpdateCustomField: (String, String, String) -> Unit,
    onRemoveCustomField: (String) -> Unit,
    onWatermarkChange: (String) -> Unit,
    onHeaderColorChange: (String) -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onNavigateToPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPhotoSelected(uri)
        }
    }

    val formImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onFormImageSelected(uri)
        }
    }

    val scrollState = rememberScrollState()
    val customFields = profile.getCustomFields()

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

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Smart Layout: Empty boxes are omitted from the PDF so no gaps appear.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // SECTION: CAPTURE OR UPLOAD FORM DOCUMENT
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(
                            text = "Physical Form / Document Image",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Capture a photo of your paper form with Camera or upload from device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Show thumbnail if form image is attached
                val formImageModel = profile.formImageUri?.let { path ->
                    if (path.startsWith("content://") || path.startsWith("file://")) path else File(path)
                }

                if (formImageModel != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp, 85.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = formImageModel,
                                contentDescription = "Captured Form Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Form Image Attached",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Saved in your dossier for reference",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        IconButton(
                            onClick = onFormImageRemoved,
                            modifier = Modifier.testTag("remove_form_image_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Form Image", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                // Buttons: Camera Capture and Gallery Upload
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRequestCameraForForm,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("capture_form_camera_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (profile.formImageUri != null) "Retake Form" else "Capture Form", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            formImagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("upload_form_image_button")
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Form", fontSize = 12.sp)
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

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Camera capture button for portrait
                        Button(
                            onClick = onRequestCameraForPhoto,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("camera_photo_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Take Photo", fontSize = 12.sp)
                        }

                        // Upload button from gallery
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pick_photo_button")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload from Gallery", fontSize = 12.sp)
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
                    label = { Text("Full Name (Header Banner & Under Photo)") },
                    placeholder = { Text("e.g. MUBARAK ADULLAHI") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("full_name_input"),
                    singleLine = true
                )

                // Photo Display Phone
                OutlinedTextField(
                    value = profile.photoDisplayNumber,
                    onValueChange = onPhotoDisplayNumberChange,
                    label = { Text("Phone Number Under Photo (Optional)") },
                    placeholder = { Text(profile.phoneNumber.ifBlank { "Leave blank to omit or use primary phone" }) },
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
                    label = { Text("LGA/State") },
                    placeholder = { Text("e.g. Kontagori, Niger State") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lga_state_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = profile.phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    label = { Text("Phone Number") },
                    placeholder = { Text("e.g. 07062270031") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_number_input"),
                    singleLine = true
                )

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
                    text = "Social Media Profiles",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Leave any platform empty to completely remove its row from the PDF.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                OutlinedTextField(
                    value = profile.facebook,
                    onValueChange = onFacebookChange,
                    label = { Text("Facebook") },
                    placeholder = { Text("e.g. Mubarak.abdullahi.739") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("facebook_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = profile.twitter,
                    onValueChange = onTwitterChange,
                    label = { Text("Twitter / X") },
                    placeholder = { Text("e.g. @mubarak_ad") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("twitter_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = profile.instagram,
                    onValueChange = onInstagramChange,
                    label = { Text("Instagram") },
                    placeholder = { Text("e.g. mubarak_adullahi") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("instagram_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = profile.youtube,
                    onValueChange = onYoutubeChange,
                    label = { Text("YouTube") },
                    placeholder = { Text("e.g. @MubarakAdullahiOfficial") },
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
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("associates_input")
                )
            }
        }

        // SECTION 4: CUSTOM FIELDS / ADDITIONAL INFORMATION
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Additional Profile Details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Add any custom information to include in the PDF",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    OutlinedButton(
                        onClick = { onAddCustomField("", "") },
                        modifier = Modifier.testTag("add_custom_field_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Field", fontSize = 12.sp)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Quick suggestions:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Party Affiliation",
                            "NIN Number",
                            "Marital Status",
                            "Blood Group",
                            "State of Origin",
                            "Religion",
                            "Languages",
                            "Website",
                            "Next of Kin"
                        ).forEach { suggestion ->
                            SuggestionChip(
                                onClick = { onAddCustomField(suggestion, "") },
                                label = { Text(suggestion, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                if (customFields.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        customFields.forEachIndexed { index, field ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_field_row_$index"),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = field.label,
                                    onValueChange = { newLabel ->
                                        onUpdateCustomField(field.id, newLabel, field.value)
                                    },
                                    label = { Text("Title / Label") },
                                    placeholder = { Text("e.g. Party") },
                                    modifier = Modifier.weight(0.42f),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = field.value,
                                    onValueChange = { newVal ->
                                        onUpdateCustomField(field.id, field.label, newVal)
                                    },
                                    label = { Text("Value") },
                                    placeholder = { Text("e.g. ADC") },
                                    modifier = Modifier.weight(0.58f),
                                    singleLine = true
                                )

                                IconButton(
                                    onClick = { onRemoveCustomField(field.id) },
                                    modifier = Modifier.size(36.dp)
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
                    value = profile.watermarkText,
                    onValueChange = onWatermarkChange,
                    label = { Text("Background Watermark Text (Optional)") },
                    placeholder = { Text("e.g. NS, CONFIDENTIAL, OFFICIAL") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("watermark_input"),
                    singleLine = true
                )

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

        Spacer(modifier = Modifier.height(30.dp))
    }
}
