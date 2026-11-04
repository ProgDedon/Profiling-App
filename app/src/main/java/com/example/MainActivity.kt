package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pdf.PdfViewerHelper
import com.example.ui.DossierViewModel
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import com.example.ui.screens.CameraCaptureScreen
import com.example.ui.screens.DesktopNavigationRail
import com.example.ui.screens.DesktopProfilesWorkspace
import com.example.ui.screens.DesktopStudioWorkspace
import com.example.ui.screens.FormScreen
import com.example.ui.screens.PreviewScreen
import com.example.ui.screens.SavedProfilesScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

enum class ScreenTab {
    FORM,
    PREVIEW,
    PROFILES
}

enum class CameraTarget {
    PROFILE_PHOTO
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var isSplashFinished by rememberSaveable { mutableStateOf(false) }

                if (!isSplashFinished) {
                    SplashScreen(
                        onSplashFinished = { isSplashFinished = true }
                    )
                } else {
                    MainAppScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: DossierViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTab by remember { mutableStateOf(ScreenTab.FORM) }
    var activeCameraTarget by remember { mutableStateOf<CameraTarget?>(null) }

    val currentProfile by viewModel.currentProfile.collectAsStateWithLifecycle()
    val savedProfiles by viewModel.savedProfiles.collectAsStateWithLifecycle()
    val isGeneratingPdf by viewModel.isGeneratingPdf.collectAsStateWithLifecycle()
    val lastGeneratedPdf by viewModel.lastGeneratedPdf.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    // Handle toast/snackbar notifications
    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Back handling
    if (activeCameraTarget == null && currentTab != ScreenTab.FORM) {
        BackHandler {
            currentTab = ScreenTab.FORM
        }
    }

    val headerColor = try {
        Color(android.graphics.Color.parseColor(currentProfile.headerColorHex))
    } catch (_: Exception) {
        Color(0xFF102E56)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isDesktop = maxWidth >= 840.dp

        Box(modifier = Modifier.fillMaxSize()) {
            if (isDesktop) {
                // Desktop Workstation Mode
                Row(modifier = Modifier.fillMaxSize()) {
                    DesktopNavigationRail(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                        savedCount = savedProfiles.size,
                        onNewProfile = {
                            viewModel.resetForm()
                            currentTab = ScreenTab.FORM
                        },
                        onQuickGeneratePdf = {
                            viewModel.generatePdf(context) { file ->
                                PdfViewerHelper.openPdf(context, file)
                            }
                        },
                        onQuickPrint = {
                            lastGeneratedPdf?.let { PdfViewerHelper.printPdf(context, it) } ?: viewModel.generatePdf(context) { file ->
                                PdfViewerHelper.printPdf(context, file)
                            }
                        },
                        headerColor = headerColor
                    )

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        when (currentTab) {
                            ScreenTab.PROFILES -> {
                                DesktopProfilesWorkspace(
                                    profiles = savedProfiles,
                                    currentProfileId = currentProfile.id,
                                    onSelectProfile = { p ->
                                        viewModel.selectProfile(p)
                                        currentTab = ScreenTab.FORM
                                    },
                                    onDuplicateProfile = viewModel::duplicateProfile,
                                    onDeleteProfile = viewModel::deleteProfile,
                                    onGeneratePdfForProfile = { p ->
                                        viewModel.selectProfile(p)
                                        viewModel.generatePdf(context) { file ->
                                            PdfViewerHelper.openPdf(context, file)
                                        }
                                    },
                                    onAddNewProfile = {
                                        viewModel.resetForm()
                                        currentTab = ScreenTab.FORM
                                    },
                                    onPrintProfile = { p ->
                                        viewModel.selectProfile(p)
                                        viewModel.generatePdf(context) { file ->
                                            PdfViewerHelper.printPdf(context, file)
                                        }
                                    },
                                    onShareProfile = { p ->
                                        viewModel.selectProfile(p)
                                        viewModel.generatePdf(context) { file ->
                                            PdfViewerHelper.sharePdf(context, file)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                DesktopStudioWorkspace(
                                    profile = currentProfile,
                                    isGeneratingPdf = isGeneratingPdf,
                                    lastPdfFile = lastGeneratedPdf,
                                    onFullNameChange = viewModel::updateFullName,
                                    onPhotoSelected = { uri -> viewModel.setPhotoFromUri(context, uri) },
                                    onPhotoRemoved = viewModel::removePhoto,
                                    onRequestCameraForPhoto = { activeCameraTarget = CameraTarget.PROFILE_PHOTO },
                                    onGenderChange = viewModel::updateGender,
                                    onLgaStateChange = viewModel::updateLgaState,
                                    onAddressChange = viewModel::updateAddress,
                                    onPhoneNumberChange = viewModel::updatePhoneNumber,
                                    onAlsoKnownAsChange = viewModel::updateAlsoKnownAs,
                                    onDateOfBirthChange = viewModel::updateDateOfBirth,
                                    onFacebookChange = viewModel::updateFacebook,
                                    onTwitterChange = viewModel::updateTwitter,
                                    onInstagramChange = viewModel::updateInstagram,
                                    onTiktokChange = viewModel::updateTiktok,
                                    onYoutubeChange = viewModel::updateYoutube,
                                    onEmailChange = viewModel::updateEmail,
                                    onWebsitesChange = viewModel::updateWebsites,
                                    onOtherSocialChange = viewModel::updateOtherSocial,
                                    onOccupationChange = viewModel::updateOccupation,
                                    onEducationChange = viewModel::updateEducation,
                                    onAssociatesChange = viewModel::updateAssociates,
                                    onAddPersonalInfoField = viewModel::addPersonalInfoField,
                                    onUpdatePersonalInfoField = viewModel::updatePersonalInfoField,
                                    onRemovePersonalInfoField = viewModel::removePersonalInfoField,
                                    onAddOtherInfoField = viewModel::addOtherInfoField,
                                    onUpdateOtherInfoField = viewModel::updateOtherInfoField,
                                    onRemoveOtherInfoField = viewModel::removeOtherInfoField,
                                    onWatermarkChange = viewModel::updateWatermarkText,
                                    onHeaderColorChange = viewModel::updateHeaderColor,
                                    onReset = viewModel::resetForm,
                                    onSave = viewModel::saveCurrentProfile,
                                    onGenerateAndOpen = {
                                        viewModel.generatePdf(context) { file ->
                                            PdfViewerHelper.openPdf(context, file)
                                        }
                                    },
                                    onDownload = {
                                        viewModel.saveToDownloads(context)
                                    },
                                    onShare = {
                                        lastGeneratedPdf?.let { file ->
                                            PdfViewerHelper.sharePdf(context, file)
                                        } ?: viewModel.generatePdf(context) { file ->
                                            PdfViewerHelper.sharePdf(context, file)
                                        }
                                    },
                                    onPrint = {
                                        lastGeneratedPdf?.let { file ->
                                            PdfViewerHelper.printPdf(context, file)
                                        } ?: viewModel.generatePdf(context) { file ->
                                            PdfViewerHelper.printPdf(context, file)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            } else {
                // Handheld / Mobile Mode
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.owl_logo_icon_1793603390711),
                                        contentDescription = "greymmc Owl Logo",
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    )
                                    Text(
                                        text = when (currentTab) {
                                            ScreenTab.FORM -> "Profile Builder"
                                            ScreenTab.PREVIEW -> "PDF Document"
                                            ScreenTab.PROFILES -> "Saved Profiles"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = headerColor),
                            actions = {
                                if (currentTab == ScreenTab.FORM) {
                                    IconButton(
                                        onClick = { activeCameraTarget = CameraTarget.PROFILE_PHOTO },
                                        modifier = Modifier.testTag("top_camera_action")
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.White)
                                    }
                                } else if (currentTab == ScreenTab.PREVIEW) {
                                    IconButton(
                                        onClick = {
                                            lastGeneratedPdf?.let { file ->
                                                PdfViewerHelper.sharePdf(context, file)
                                            } ?: viewModel.generatePdf(context) { file ->
                                                PdfViewerHelper.sharePdf(context, file)
                                            }
                                        },
                                        modifier = Modifier.testTag("top_share_action")
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Share PDF", tint = Color.White)
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentTab == ScreenTab.FORM,
                                onClick = { currentTab = ScreenTab.FORM },
                                icon = { Icon(Icons.Default.Edit, contentDescription = "Form") },
                                label = { Text("Form") },
                                modifier = Modifier.testTag("nav_form_tab")
                            )
                            NavigationBarItem(
                                selected = currentTab == ScreenTab.PREVIEW,
                                onClick = { currentTab = ScreenTab.PREVIEW },
                                icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = "Preview & PDF") },
                                label = { Text("PDF View") },
                                modifier = Modifier.testTag("nav_preview_tab")
                            )
                            NavigationBarItem(
                                selected = currentTab == ScreenTab.PROFILES,
                                onClick = { currentTab = ScreenTab.PROFILES },
                                icon = { Icon(Icons.Default.Badge, contentDescription = "Saved Dossiers") },
                                label = { Text("Saved (${savedProfiles.size})") },
                                modifier = Modifier.testTag("nav_profiles_tab")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            ScreenTab.FORM -> {
                                FormScreen(
                                    profile = currentProfile,
                                    onFullNameChange = viewModel::updateFullName,
                                    onPhotoSelected = { uri -> viewModel.setPhotoFromUri(context, uri) },
                                    onPhotoRemoved = viewModel::removePhoto,
                                    onRequestCameraForPhoto = { activeCameraTarget = CameraTarget.PROFILE_PHOTO },
                                    onGenderChange = viewModel::updateGender,
                                    onLgaStateChange = viewModel::updateLgaState,
                                    onAddressChange = viewModel::updateAddress,
                                    onPhoneNumberChange = viewModel::updatePhoneNumber,
                                    onAlsoKnownAsChange = viewModel::updateAlsoKnownAs,
                                    onDateOfBirthChange = viewModel::updateDateOfBirth,
                                    onFacebookChange = viewModel::updateFacebook,
                                    onTwitterChange = viewModel::updateTwitter,
                                    onInstagramChange = viewModel::updateInstagram,
                                    onTiktokChange = viewModel::updateTiktok,
                                    onYoutubeChange = viewModel::updateYoutube,
                                    onEmailChange = viewModel::updateEmail,
                                    onWebsitesChange = viewModel::updateWebsites,
                                    onOtherSocialChange = viewModel::updateOtherSocial,
                                    onOccupationChange = viewModel::updateOccupation,
                                    onEducationChange = viewModel::updateEducation,
                                    onAssociatesChange = viewModel::updateAssociates,
                                    onAddPersonalInfoField = viewModel::addPersonalInfoField,
                                    onUpdatePersonalInfoField = viewModel::updatePersonalInfoField,
                                    onRemovePersonalInfoField = viewModel::removePersonalInfoField,
                                    onAddOtherInfoField = viewModel::addOtherInfoField,
                                    onUpdateOtherInfoField = viewModel::updateOtherInfoField,
                                    onRemoveOtherInfoField = viewModel::removeOtherInfoField,
                                    onWatermarkChange = viewModel::updateWatermarkText,
                                    onHeaderColorChange = viewModel::updateHeaderColor,
                                    onReset = viewModel::resetForm,
                                    onSave = viewModel::saveCurrentProfile,
                                    onNavigateToPreview = { currentTab = ScreenTab.PREVIEW }
                                )
                            }

                            ScreenTab.PREVIEW -> {
                                PreviewScreen(
                                    profile = currentProfile,
                                    isGenerating = isGeneratingPdf,
                                    lastPdfFile = lastGeneratedPdf,
                                    onGenerateAndOpen = {
                                        viewModel.generatePdf(context) { file ->
                                            PdfViewerHelper.openPdf(context, file)
                                        }
                                    },
                                    onSharePdf = { file ->
                                        PdfViewerHelper.sharePdf(context, file)
                                    },
                                    onPrintPdf = { file ->
                                        PdfViewerHelper.printPdf(context, file)
                                    },
                                    onSaveToDownloads = {
                                        viewModel.saveToDownloads(context)
                                    },
                                    onEditProfile = {
                                        currentTab = ScreenTab.FORM
                                    }
                                )
                            }

                            ScreenTab.PROFILES -> {
                                SavedProfilesScreen(
                                    profiles = savedProfiles,
                                    currentProfileId = currentProfile.id,
                                    onSelectProfile = { profile ->
                                        viewModel.selectProfile(profile)
                                        currentTab = ScreenTab.FORM
                                    },
                                    onDuplicateProfile = viewModel::duplicateProfile,
                                    onDeleteProfile = viewModel::deleteProfile,
                                    onGeneratePdfForProfile = { profile ->
                                        viewModel.selectProfile(profile)
                                        viewModel.generatePdf(context) { file ->
                                            PdfViewerHelper.openPdf(context, file)
                                        }
                                        currentTab = ScreenTab.PREVIEW
                                    },
                                    onAddNewProfile = {
                                        viewModel.resetForm()
                                        currentTab = ScreenTab.FORM
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Camera Screen Overlay
            activeCameraTarget?.let { target ->
                CameraCaptureScreen(
                    onPhotoCaptured = { photoPath ->
                        when (target) {
                            CameraTarget.PROFILE_PHOTO -> viewModel.setPhotoFromPath(photoPath)
                        }
                        activeCameraTarget = null
                    },
                    onClose = { activeCameraTarget = null }
                )
            }
        }
    }
}
