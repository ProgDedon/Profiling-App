package com.example.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

data class DesktopProfile(
    val id: Long = System.currentTimeMillis(),
    val fullName: String = "",
    val gender: String = "Male",
    val lgaState: String = "",
    val address: String = "",
    val phoneNumber: String = "",
    val alsoKnownAs: String = "",
    val dateOfBirth: String = "",
    val facebook: String = "",
    val twitter: String = "",
    val instagram: String = "",
    val tiktok: String = "",
    val youtube: String = "",
    val email: String = "",
    val websites: String = "",
    val otherSocialMedia: String = "",
    val occupation: String = "",
    val education: String = "",
    val associatesPhoneNumbers: String = "",
    val customPersonalFields: List<Pair<String, String>> = emptyList(),
    val customOtherFields: List<Pair<String, String>> = emptyList()
)

val BrandGold = Color(0xFFC5A059)
val BrandNavy = Color(0xFF1E293B)
val BrandSurface = Color(0xFFF8FAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopAppUI() {
    var currentTab by remember { mutableStateOf(0) } // 0: Studio, 1: Profiles
    var profile by remember {
        mutableStateOf(
            DesktopProfile(
                fullName = "Dr. Alexander Maximillian",
                gender = "Male",
                lgaState = "Ikeja, Lagos State",
                address = "Plot 42, Innovation Hub Avenue, Victoria Island, Lagos, Nigeria",
                phoneNumber = "+2348011112222, +2348033334444",
                alsoKnownAs = "The Architect",
                dateOfBirth = "14th April 1980",
                facebook = "alex.max.official",
                twitter = "@alex_arch",
                instagram = "@alex.maximillian",
                tiktok = "@alex_tok",
                youtube = "youtube.com/@alexmax",
                email = "alex.maximillian@greymmc.org",
                websites = "https://alexmaximillian.org",
                otherSocialMedia = "LinkedIn: in/alexmax, Telegram: @alex_max",
                occupation = "Managing Director & Senior Consultant",
                education = "Ph.D. in Computer Engineering, M.Sc Software Systems",
                associatesPhoneNumbers = "+2348099998888 (Executive Assistant)\n+2348077776666 (Chief of Staff)"
            )
        )
    }
    var savedProfiles by remember { mutableStateOf(listOf(profile)) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = BrandGold,
            onPrimary = Color.Black,
            surface = Color.White,
            background = BrandSurface
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = BrandSurface) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Desktop Navigation Rail
                NavigationRail(
                    containerColor = Color.White,
                    modifier = Modifier.width(96.dp).fillMaxHeight()
                ) {
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(BrandNavy)
                            .border(2.dp, BrandGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🦉", fontSize = 26.sp)
                    }
                    Text(
                        "greymmc",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = BrandNavy,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    NavigationRailItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = { Icon(Icons.Default.Edit, contentDescription = "Studio") },
                        label = { Text("Studio") }
                    )
                    NavigationRailItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Dossiers") },
                        label = { Text("Profiles") }
                    )

                    Spacer(Modifier.weight(1f))

                    IconButton(
                        onClick = {
                            profile = DesktopProfile()
                            currentTab = 0
                            notificationMessage = "New clean profile opened"
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Profile", tint = BrandNavy)
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color(0xFFE2E8F0))

                // Main Content Workspace
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (currentTab == 0) {
                        DesktopStudioWorkspace(
                            profile = profile,
                            onProfileChange = { profile = it },
                            onSaveProfile = {
                                savedProfiles = (savedProfiles.filter { p -> p.id != profile.id } + profile)
                                notificationMessage = "Saved profile for ${profile.fullName.ifBlank { "Untitled" }}"
                            },
                            onExportPdf = {
                                scope.launch {
                                    val chooser = JFileChooser()
                                    chooser.dialogTitle = "Save A4 Landscape Dossier PDF"
                                    chooser.fileFilter = FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf")
                                    chooser.selectedFile = File("${profile.fullName.replace(" ", "_").ifBlank { "Dossier" }}_A4_Landscape.pdf")
                                    val result = chooser.showSaveDialog(null)
                                    if (result == JFileChooser.APPROVE_OPTION) {
                                        var targetFile = chooser.selectedFile
                                        if (!targetFile.name.lowercase().endsWith(".pdf")) {
                                            targetFile = File(targetFile.absolutePath + ".pdf")
                                        }
                                        DesktopPdfExporter.exportA4Landscape(profile, targetFile)
                                        notificationMessage = "PDF exported successfully to: ${targetFile.name}"
                                    }
                                }
                            }
                        )
                    } else {
                        DesktopProfilesWorkspace(
                            profiles = savedProfiles,
                            onSelectProfile = {
                                profile = it
                                currentTab = 0
                                notificationMessage = "Loaded profile: ${it.fullName}"
                            },
                            onDeleteProfile = { toDelete ->
                                savedProfiles = savedProfiles.filter { it.id != toDelete.id }
                                notificationMessage = "Removed profile"
                            }
                        )
                    }

                    // Floating Notification Snackbar
                    notificationMessage?.let { msg ->
                        Snackbar(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(24.dp)
                                .widthIn(max = 420.dp),
                            action = {
                                TextButton(onClick = { notificationMessage = null }) {
                                    Text("Dismiss", color = BrandGold)
                                }
                            }
                        ) {
                            Text(msg)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DesktopStudioWorkspace(
    profile: DesktopProfile,
    onProfileChange: (DesktopProfile) -> Unit,
    onSaveProfile: () -> Unit,
    onExportPdf: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left Column: Form Editor (480dp)
        Column(
            modifier = Modifier
                .width(480.dp)
                .fillMaxHeight()
                .background(Color.White)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Profile Editor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = BrandNavy
                )
                Button(
                    onClick = onSaveProfile,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Save", color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("PERSONAL & CONTACT INFORMATION", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandGold)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = profile.fullName,
                onValueChange = { onProfileChange(profile.copy(fullName = it)) },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = profile.address,
                onValueChange = { onProfileChange(profile.copy(address = it)) },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = profile.gender,
                    onValueChange = { onProfileChange(profile.copy(gender = it)) },
                    label = { Text("Gender") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = profile.lgaState,
                    onValueChange = { onProfileChange(profile.copy(lgaState = it)) },
                    label = { Text("LGA & State") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = profile.phoneNumber,
                onValueChange = { onProfileChange(profile.copy(phoneNumber = it)) },
                label = { Text("Phone Number(s)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = profile.alsoKnownAs,
                    onValueChange = { onProfileChange(profile.copy(alsoKnownAs = it)) },
                    label = { Text("Also Known As (AKA)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = profile.dateOfBirth,
                    onValueChange = { onProfileChange(profile.copy(dateOfBirth = it)) },
                    label = { Text("Date of Birth") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("SOCIAL MEDIA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandGold)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = profile.facebook,
                    onValueChange = { onProfileChange(profile.copy(facebook = it)) },
                    label = { Text("Facebook") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = profile.twitter,
                    onValueChange = { onProfileChange(profile.copy(twitter = it)) },
                    label = { Text("Twitter / X") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = profile.instagram,
                    onValueChange = { onProfileChange(profile.copy(instagram = it)) },
                    label = { Text("Instagram") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = profile.tiktok,
                    onValueChange = { onProfileChange(profile.copy(tiktok = it)) },
                    label = { Text("TikTok") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = profile.youtube,
                onValueChange = { onProfileChange(profile.copy(youtube = it)) },
                label = { Text("YouTube") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = profile.email,
                onValueChange = { onProfileChange(profile.copy(email = it)) },
                label = { Text("Email Address(es)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = profile.websites,
                onValueChange = { onProfileChange(profile.copy(websites = it)) },
                label = { Text("Websites") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = profile.otherSocialMedia,
                onValueChange = { onProfileChange(profile.copy(otherSocialMedia = it)) },
                label = { Text("Other Social Media (LinkedIn, Telegram)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Text("OTHER INFORMATION", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandGold)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = profile.occupation,
                onValueChange = { onProfileChange(profile.copy(occupation = it)) },
                label = { Text("Occupation/ Role") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = profile.education,
                onValueChange = { onProfileChange(profile.copy(education = it)) },
                label = { Text("Education / Qualifications") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = profile.associatesPhoneNumbers,
                onValueChange = { onProfileChange(profile.copy(associatesPhoneNumbers = it)) },
                label = { Text("Associates / Emergency Contacts") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color(0xFFE2E8F0))

        // Right Column: Live A4 Landscape Document Preview & Controls
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Live A4 Landscape Preview",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = BrandNavy
                    )
                    Text(
                        "Bookman Old Style Typography • 842 x 595 pt Layout",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = onExportPdf,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGold)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("Export Windows A4 PDF", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            // A4 Landscape Document Card Canvas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(BrandNavy)
                                    .border(2.dp, BrandGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🦉", fontSize = 20.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    profile.fullName.ifBlank { "SUBJECT FULL NAME" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = BrandNavy,
                                    fontFamily = FontFamily.Serif
                                )
                                Text(
                                    "EXECUTIVE DOSSIER REPORT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandGold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                        ) {
                            Text(
                                "CONFIDENTIAL",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    Divider(color = BrandGold, thickness = 2.dp)
                    Spacer(Modifier.height(16.dp))

                    // Preview Sections Table
                    PreviewTableSection("PERSONAL & CONTACT INFORMATION", listOf(
                        "Full Name" to profile.fullName,
                        "Address" to profile.address,
                        "Gender" to profile.gender,
                        "LGA & State" to profile.lgaState,
                        "Phone Number" to profile.phoneNumber,
                        "Also Known As" to profile.alsoKnownAs,
                        "Date of Birth" to profile.dateOfBirth
                    ))

                    Spacer(Modifier.height(14.dp))
                    PreviewTableSection("SOCIAL MEDIA", listOf(
                        "Facebook" to profile.facebook,
                        "Twitter / X" to profile.twitter,
                        "Instagram" to profile.instagram,
                        "TikTok" to profile.tiktok,
                        "YouTube" to profile.youtube,
                        "Email" to profile.email,
                        "Websites" to profile.websites,
                        "Other Social Media" to profile.otherSocialMedia
                    ))

                    Spacer(Modifier.height(14.dp))
                    PreviewTableSection("OTHER INFORMATION", listOf(
                        "Occupation/ Role" to profile.occupation,
                        "Education / Qualifications" to profile.education,
                        "Associates Phone Numbers" to profile.associatesPhoneNumbers
                    ))
                }
            }
        }
    }
}

@Composable
fun PreviewTableSection(title: String, rows: List<Pair<String, String>>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandNavy)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(title, color = BrandGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        rows.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color(0xFFE2E8F0))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(pair[0].first, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    Text(pair[0].second.ifBlank { "—" }, fontSize = 13.sp, color = BrandNavy, fontFamily = FontFamily.Serif)
                }
                if (pair.size > 1) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(pair[1].first, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Text(pair[1].second.ifBlank { "—" }, fontSize = 13.sp, color = BrandNavy, fontFamily = FontFamily.Serif)
                    }
                }
            }
        }
    }
}

@Composable
fun DesktopProfilesWorkspace(
    profiles: List<DesktopProfile>,
    onSelectProfile: (DesktopProfile) -> Unit,
    onDeleteProfile: (DesktopProfile) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Dossier Archives", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = BrandNavy)
        Text("Saved profiles available for export and editing", fontSize = 13.sp, color = Color.Gray)
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(profiles) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSelectProfile(item) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.fullName.ifBlank { "Untitled Profile" }, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrandNavy)
                            Text("${item.occupation.ifBlank { "Unspecified Role" }} • ${item.lgaState.ifBlank { "Nigeria" }}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Row {
                            Button(onClick = { onSelectProfile(item) }) {
                                Text("Open in Studio")
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { onDeleteProfile(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}
