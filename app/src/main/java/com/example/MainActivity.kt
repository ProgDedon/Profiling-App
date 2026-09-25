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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pdf.PdfViewerHelper
import com.example.ui.DossierViewModel
import com.example.ui.screens.FormScreen
import com.example.ui.screens.PreviewScreen
import com.example.ui.screens.SavedProfilesScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

enum class ScreenTab {
    FORM,
    PREVIEW,
    PROFILES
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
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
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTab by remember { mutableStateOf(ScreenTab.FORM) }

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
    if (currentTab != ScreenTab.FORM) {
        BackHandler {
            currentTab = ScreenTab.FORM
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            ScreenTab.FORM -> "Fill Dossier Info"
                            ScreenTab.PREVIEW -> "Document & PDF Preview"
                            ScreenTab.PROFILES -> "Saved Profiles"
                        },
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.generatePdf(context) { file ->
                                PdfViewerHelper.sharePdf(context, file, currentProfile.fullName)
                            }
                        },
                        modifier = Modifier.testTag("topbar_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share PDF")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == ScreenTab.FORM,
                    onClick = { currentTab = ScreenTab.FORM },
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Fill Info") },
                    label = { Text("Fill Info") },
                    modifier = Modifier.testTag("nav_form_tab")
                )
                NavigationBarItem(
                    selected = currentTab == ScreenTab.PREVIEW,
                    onClick = { currentTab = ScreenTab.PREVIEW },
                    icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = "Preview & PDF") },
                    label = { Text("Preview") },
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
                        onPhotoDisplayNumberChange = viewModel::updatePhotoDisplayNumber,
                        onGenderChange = viewModel::updateGender,
                        onLgaStateChange = viewModel::updateLgaState,
                        onPhoneNumberChange = viewModel::updatePhoneNumber,
                        onAlsoKnownAsChange = viewModel::updateAlsoKnownAs,
                        onDateOfBirthChange = viewModel::updateDateOfBirth,
                        onFacebookChange = viewModel::updateFacebook,
                        onOtherSocialChange = viewModel::updateOtherSocial,
                        onOccupationChange = viewModel::updateOccupation,
                        onEducationChange = viewModel::updateEducation,
                        onAssociatesChange = viewModel::updateAssociates,
                        onWatermarkChange = viewModel::updateWatermarkText,
                        onHeaderColorChange = viewModel::updateHeaderColor,
                        onLoadSample = viewModel::loadSampleData,
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
                        onSharePdf = {
                            viewModel.generatePdf(context) { file ->
                                PdfViewerHelper.sharePdf(context, file, currentProfile.fullName)
                            }
                        },
                        onSaveToDownloads = {
                            viewModel.saveToDownloads(context)
                        },
                        onPrintPdf = {
                            viewModel.generatePdf(context) { file ->
                                PdfViewerHelper.printPdf(context, file, currentProfile.fullName)
                            }
                        },
                        onNavigateBackToForm = { currentTab = ScreenTab.FORM }
                    )
                }

                ScreenTab.PROFILES -> {
                    SavedProfilesScreen(
                        profiles = savedProfiles,
                        selectedProfileId = currentProfile.id,
                        onSelectProfile = { profile ->
                            viewModel.selectProfile(profile)
                            currentTab = ScreenTab.FORM
                        },
                        onEditProfile = { profile ->
                            viewModel.selectProfile(profile)
                            currentTab = ScreenTab.FORM
                        },
                        onPreviewProfile = { profile ->
                            viewModel.selectProfile(profile)
                            currentTab = ScreenTab.PREVIEW
                        },
                        onDuplicateProfile = viewModel::duplicateProfile,
                        onDeleteProfile = viewModel::deleteProfile,
                        onCreateNew = {
                            viewModel.resetForm()
                            currentTab = ScreenTab.FORM
                        }
                    )
                }
            }
        }
    }
}
