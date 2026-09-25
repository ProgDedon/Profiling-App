package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DossierProfile
import java.io.File

@Composable
fun SavedProfilesScreen(
    profiles: List<DossierProfile>,
    selectedProfileId: Long,
    onSelectProfile: (DossierProfile) -> Unit,
    onEditProfile: (DossierProfile) -> Unit,
    onPreviewProfile: (DossierProfile) -> Unit,
    onDuplicateProfile: (DossierProfile) -> Unit,
    onDeleteProfile: (DossierProfile) -> Unit,
    onCreateNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var profileToDelete by remember { mutableStateOf<DossierProfile?>(null) }

    val filteredProfiles = remember(profiles, searchQuery) {
        if (searchQuery.isBlank()) {
            profiles
        } else {
            val q = searchQuery.trim().lowercase()
            profiles.filter {
                it.fullName.lowercase().contains(q) ||
                        it.phoneNumber.contains(q) ||
                        it.lgaState.lowercase().contains(q) ||
                        it.occupation.lowercase().contains(q) ||
                        it.alsoKnownAs.lowercase().contains(q)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Profiles") },
                placeholder = { Text("Search by name, phone, LGA, or occupation") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_profiles_input")
            )

            // Header summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredProfiles.size} Saved Dossier${if (filteredProfiles.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedButton(
                    onClick = onCreateNew,
                    modifier = Modifier.testTag("create_new_profile_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Profile", fontSize = 12.sp)
                }
            }

            if (filteredProfiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching profiles found" else "No saved profiles yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onCreateNew,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Create First Profile")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProfiles, key = { it.id }) { profile ->
                        val isCurrent = profile.id == selectedProfileId
                        ProfileCardItem(
                            profile = profile,
                            isSelected = isCurrent,
                            onCardClick = { onSelectProfile(profile) },
                            onEdit = { onEditProfile(profile) },
                            onPreview = { onPreviewProfile(profile) },
                            onDuplicate = { onDuplicateProfile(profile) },
                            onDelete = { profileToDelete = profile }
                        )
                    }
                }
            }
        }

        // Floating Action Button to create new
        FloatingActionButton(
            onClick = onCreateNew,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_profile")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add New Profile")
        }

        // Delete Confirmation Dialog
        if (profileToDelete != null) {
            AlertDialog(
                onDismissRequest = { profileToDelete = null },
                title = { Text("Delete Profile") },
                text = { Text("Are you sure you want to delete the profile for \"${profileToDelete?.fullName}\"? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            profileToDelete?.let { onDeleteProfile(it) }
                            profileToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
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
}

@Composable
private fun ProfileCardItem(
    profile: DossierProfile,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    onEdit: () -> Unit,
    onPreview: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("profile_card_${profile.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar / Thumbnail
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE2E8F0))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
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
                        val initials = profile.fullName.split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .joinToString("") { it.take(1).uppercase() }
                            .ifEmpty { "?" }
                        Text(
                            text = initials,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Profile Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.fullName.ifBlank { "Untitled Profile" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (profile.phoneNumber.isNotBlank() || profile.lgaState.isNotBlank()) {
                        Text(
                            text = listOfNotNull(
                                profile.phoneNumber.takeIf { it.isNotBlank() },
                                profile.lgaState.takeIf { it.isNotBlank() }
                            ).joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (profile.occupation.isNotBlank()) {
                        Text(
                            text = profile.occupation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }
            }

            // Quick Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreview, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Visibility, contentDescription = "Preview Document", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = onDuplicate, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate Profile", tint = MaterialTheme.colorScheme.outline)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Profile", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
