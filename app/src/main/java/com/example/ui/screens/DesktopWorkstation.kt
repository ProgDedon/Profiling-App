package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ScreenTab
import com.example.data.model.DossierProfile
import com.example.pdf.PdfViewerHelper
import com.example.ui.components.BookmanFontFamily
import com.example.ui.components.DossierDocumentPreview
import java.io.File

@Composable
fun DesktopNavigationRail(
    currentTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit,
    savedCount: Int,
    onNewProfile: () -> Unit,
    onQuickGeneratePdf: () -> Unit,
    onQuickPrint: () -> Unit,
    headerColor: Color,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier
            .width(88.dp)
            .fillMaxHeight(),
        containerColor = headerColor,
        contentColor = Color.White,
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.owl_logo_icon_1793603390711),
                    contentDescription = "Owl Logo",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "greymmc",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = BookmanFontFamily
                )
                Text(
                    text = "DESKTOP",
                    color = Color(0xFF93C5FD),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    ) {
        val itemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = headerColor,
            selectedTextColor = Color.White,
            indicatorColor = Color.White,
            unselectedIconColor = Color(0xFFCBD5E1),
            unselectedTextColor = Color(0xFFCBD5E1)
        )

        NavigationRailItem(
            selected = currentTab != ScreenTab.PROFILES,
            onClick = { onTabSelected(ScreenTab.FORM) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Studio Workspace") },
            label = { Text("Studio", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = itemColors,
            modifier = Modifier.testTag("desktop_nav_studio")
        )

        Spacer(modifier = Modifier.height(8.dp))

        NavigationRailItem(
            selected = currentTab == ScreenTab.PROFILES,
            onClick = { onTabSelected(ScreenTab.PROFILES) },
            icon = { Icon(Icons.Default.Badge, contentDescription = "Saved Dossiers") },
            label = { Text("Saved ($savedCount)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
            colors = itemColors,
            modifier = Modifier.testTag("desktop_nav_saved")
        )

        Spacer(modifier = Modifier.weight(1f))

        // Desktop Quick Actions at the bottom of the rail
        IconButton(
            onClick = onNewProfile,
            modifier = Modifier.testTag("desktop_rail_new_profile")
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Dossier", tint = Color.White)
        }

        IconButton(
            onClick = onQuickGeneratePdf,
            modifier = Modifier.testTag("desktop_rail_export_pdf")
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = Color.White)
        }

        IconButton(
            onClick = onQuickPrint,
            modifier = Modifier.testTag("desktop_rail_print")
        ) {
            Icon(Icons.Default.Print, contentDescription = "Print PDF", tint = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DesktopStudioWorkspace(
    profile: DossierProfile,
    isGeneratingPdf: Boolean,
    lastPdfFile: File?,
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
    onGenerateAndOpen: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onPrint: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxSize()) {
        // Left Pane: Form Editor (470dp wide)
        Column(
            modifier = Modifier
                .width(470.dp)
                .fillMaxHeight()
                .background(Color(0xFFF8FAFC))
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "PROFILE EDITOR",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = BookmanFontFamily
                        )
                        Text(
                            text = "Edits sync to preview instantly",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onSave,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("desktop_save_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onReset,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("desktop_reset_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New", fontSize = 12.sp)
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                FormScreen(
                    profile = profile,
                    onFullNameChange = onFullNameChange,
                    onPhotoSelected = onPhotoSelected,
                    onPhotoRemoved = onPhotoRemoved,
                    onRequestCameraForPhoto = onRequestCameraForPhoto,
                    onGenderChange = onGenderChange,
                    onLgaStateChange = onLgaStateChange,
                    onAddressChange = onAddressChange,
                    onPhoneNumberChange = onPhoneNumberChange,
                    onAlsoKnownAsChange = onAlsoKnownAsChange,
                    onDateOfBirthChange = onDateOfBirthChange,
                    onFacebookChange = onFacebookChange,
                    onTwitterChange = onTwitterChange,
                    onInstagramChange = onInstagramChange,
                    onTiktokChange = onTiktokChange,
                    onYoutubeChange = onYoutubeChange,
                    onEmailChange = onEmailChange,
                    onWebsitesChange = onWebsitesChange,
                    onOtherSocialChange = onOtherSocialChange,
                    onOccupationChange = onOccupationChange,
                    onEducationChange = onEducationChange,
                    onAssociatesChange = onAssociatesChange,
                    onAddPersonalInfoField = onAddPersonalInfoField,
                    onUpdatePersonalInfoField = onUpdatePersonalInfoField,
                    onRemovePersonalInfoField = onRemovePersonalInfoField,
                    onAddOtherInfoField = onAddOtherInfoField,
                    onUpdateOtherInfoField = onUpdateOtherInfoField,
                    onRemoveOtherInfoField = onRemoveOtherInfoField,
                    onWatermarkChange = onWatermarkChange,
                    onHeaderColorChange = onHeaderColorChange,
                    onReset = onReset,
                    onSave = onSave,
                    onNavigateToPreview = {},
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Vertical divider separating the two panes
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(Color(0xFFCBD5E1))
        )

        // Right Pane: Live A4 Landscape Preview Studio (Flexible Width)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFFF1F5F9))
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "LIVE A4 LANDSCAPE DOSSIER",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = BookmanFontFamily
                            )
                            Text(
                                text = "842 × 595 pt • Dynamic Multi-Page Engine",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // Desktop Studio Action Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onGenerateAndOpen,
                            enabled = !isGeneratingPdf,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("desktop_generate_open_pdf")
                        ) {
                            if (isGeneratingPdf) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Building PDF...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = onDownload,
                            enabled = !isGeneratingPdf,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("desktop_download_pdf")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onPrint,
                            enabled = !isGeneratingPdf,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("desktop_print_pdf")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Print", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onShare,
                            enabled = !isGeneratingPdf,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("desktop_share_pdf")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Scrollable canvas showing the full A4 landscape document
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                DossierDocumentPreview(
                    profile = profile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 842.dp)
                )
            }
        }
    }
}

@Composable
fun DesktopProfilesWorkspace(
    profiles: List<DossierProfile>,
    currentProfileId: Long,
    onSelectProfile: (DossierProfile) -> Unit,
    onDuplicateProfile: (DossierProfile) -> Unit,
    onDeleteProfile: (DossierProfile) -> Unit,
    onGeneratePdfForProfile: (DossierProfile) -> Unit,
    onAddNewProfile: () -> Unit,
    onPrintProfile: (DossierProfile) -> Unit,
    onShareProfile: (DossierProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProfile by remember(profiles, currentProfileId) {
        mutableStateOf(profiles.find { it.id == currentProfileId } ?: profiles.firstOrNull())
    }
    var profileToDelete by remember { mutableStateOf<DossierProfile?>(null) }

    val filteredProfiles = remember(profiles, searchQuery) {
        if (searchQuery.isBlank()) {
            profiles
        } else {
            profiles.filter {
                it.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.phoneNumber.contains(searchQuery, ignoreCase = true) ||
                    it.lgaState.contains(searchQuery, ignoreCase = true) ||
                    it.alsoKnownAs.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Row(modifier = modifier.fillMaxSize()) {
        // Left Pane: Search and List of profiles (380dp)
        Column(
            modifier = Modifier
                .width(380.dp)
                .fillMaxHeight()
                .background(Color(0xFFF8FAFC))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "SAVED DOSSIERS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = BookmanFontFamily
                    )
                    Text(
                        text = "${filteredProfiles.size} total dossiers",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Button(
                    onClick = onAddNewProfile,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search profiles...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("desktop_profiles_search")
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredProfiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "No saved profiles yet" else "No matching profiles found",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProfiles, key = { it.id }) { p ->
                        val isSelected = selectedProfile?.id == p.id
                        Card(
                            onClick = { selectedProfile = p },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White
                            ),
                            border = if (isSelected) {
                                androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.fullName.ifBlank { "Untitled Profile" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (p.occupation.isNotBlank()) {
                                        Text(
                                            text = p.occupation,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (p.lgaState.isNotBlank()) {
                                        Text(
                                            text = p.lgaState,
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { onDuplicateProfile(p) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = { profileToDelete = p },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Vertical Divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(Color(0xFFCBD5E1))
        )

        // Right Pane: Full Detail Preview of selected profile
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFFF1F5F9))
        ) {
            val prof = selectedProfile ?: profiles.firstOrNull()

            if (prof != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = prof.fullName.ifBlank { "DOSSIER DETAILS" }.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = BookmanFontFamily
                            )
                            Text(
                                text = "A4 Landscape Document Viewer",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onSelectProfile(prof) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit in Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onGeneratePdfForProfile(prof) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Generate PDF", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { onPrintProfile(prof) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Print", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { onShareProfile(prof) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    DossierDocumentPreview(
                        profile = prof,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 842.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Select a dossier from the left pane to preview", color = Color(0xFF64748B))
                }
            }
        }
    }

    profileToDelete?.let { p ->
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            title = { Text("Delete Dossier?") },
            text = { Text("Are you sure you want to delete '${p.fullName}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProfile(p)
                        if (selectedProfile?.id == p.id) {
                            selectedProfile = profiles.firstOrNull { it.id != p.id }
                        }
                        profileToDelete = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
