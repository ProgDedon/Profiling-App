package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.DossierProfile
import com.example.data.repository.DossierRepository
import com.example.pdf.PdfGenerator
import com.example.util.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class DossierViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DossierRepository
    val savedProfiles: StateFlow<List<DossierProfile>>

    private val _currentProfile = MutableStateFlow(DossierProfile.createEmpty())
    val currentProfile: StateFlow<DossierProfile> = _currentProfile.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    private val _lastGeneratedPdf = MutableStateFlow<File?>(null)
    val lastGeneratedPdf: StateFlow<File?> = _lastGeneratedPdf.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = DossierRepository(db.dossierDao())
        savedProfiles = repository.allProfiles.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun updateProfile(modifier: (DossierProfile) -> DossierProfile) {
        _currentProfile.value = modifier(_currentProfile.value).copy(updatedAt = System.currentTimeMillis())
    }

    fun updateFullName(value: String) = updateProfile { it.copy(fullName = value) }
    fun updateGender(value: String) = updateProfile { it.copy(gender = value) }
    fun updateLgaState(value: String) = updateProfile { it.copy(lgaState = value) }
    fun updatePhoneNumber(value: String) = updateProfile { it.copy(phoneNumber = value, photoDisplayNumber = value) }
    fun updateAlsoKnownAs(value: String) = updateProfile { it.copy(alsoKnownAs = value) }
    fun updateDateOfBirth(value: String) = updateProfile { it.copy(dateOfBirth = value) }
    fun updateFacebook(value: String) = updateProfile { it.copy(facebook = value) }
    fun updateTwitter(value: String) = updateProfile { it.copy(twitter = value) }
    fun updateInstagram(value: String) = updateProfile { it.copy(instagram = value) }
    fun updateYoutube(value: String) = updateProfile { it.copy(youtube = value) }
    fun updateOtherSocial(value: String) = updateProfile { it.copy(otherSocialMedia = value) }
    fun updateOccupation(value: String) = updateProfile { it.copy(occupation = value) }
    fun updateEducation(value: String) = updateProfile { it.copy(education = value) }
    fun updateAssociates(value: String) = updateProfile { it.copy(associatesPhoneNumbers = value) }
    fun updatePhotoDisplayNumber(value: String) = updateProfile { it.copy(photoDisplayNumber = value) }
    fun updateWatermarkText(value: String) = updateProfile { it.copy(watermarkText = value) }
    fun updateHeaderColor(hex: String) = updateProfile { it.copy(headerColorHex = hex) }

    fun addPersonalInfoField(label: String = "", value: String = "") {
        val list = _currentProfile.value.getPersonalInfoList().toMutableList()
        list.add(com.example.data.model.CustomField(label = label, value = value))
        _currentProfile.value = _currentProfile.value.withPersonalInfoList(list)
    }

    fun updatePersonalInfoField(id: String, label: String, value: String) {
        val list = _currentProfile.value.getPersonalInfoList().map { field ->
            if (field.id == id) field.copy(label = label, value = value) else field
        }
        _currentProfile.value = _currentProfile.value.withPersonalInfoList(list)
    }

    fun removePersonalInfoField(id: String) {
        val list = _currentProfile.value.getPersonalInfoList().filter { it.id != id }
        _currentProfile.value = _currentProfile.value.withPersonalInfoList(list)
    }

    fun addOtherInfoField(label: String = "", value: String = "") {
        val list = _currentProfile.value.getOtherInfoList().toMutableList()
        list.add(com.example.data.model.CustomField(label = label, value = value))
        _currentProfile.value = _currentProfile.value.withOtherInfoList(list)
    }

    fun updateOtherInfoField(id: String, label: String, value: String) {
        val list = _currentProfile.value.getOtherInfoList().map { field ->
            if (field.id == id) field.copy(label = label, value = value) else field
        }
        _currentProfile.value = _currentProfile.value.withOtherInfoList(list)
    }

    fun removeOtherInfoField(id: String) {
        val list = _currentProfile.value.getOtherInfoList().filter { it.id != id }
        _currentProfile.value = _currentProfile.value.withOtherInfoList(list)
    }

    fun setPhotoFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val localPath = ImageUtils.saveImageLocally(context, uri)
            if (localPath != null) {
                updateProfile { it.copy(photoUri = localPath) }
            } else {
                _statusMessage.value = "Failed to load selected photo"
            }
        }
    }

    fun setPhotoFromPath(path: String) {
        updateProfile { it.copy(photoUri = path) }
        _statusMessage.value = "Photo captured successfully!"
    }

    fun removePhoto() {
        updateProfile { it.copy(photoUri = null) }
    }

    fun setFormImageFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val localPath = ImageUtils.saveImageLocally(context, uri)
            if (localPath != null) {
                updateProfile { it.copy(formImageUri = localPath) }
                _statusMessage.value = "Form image uploaded successfully!"
            } else {
                _statusMessage.value = "Failed to upload form image"
            }
        }
    }

    fun setFormImageFromPath(path: String) {
        updateProfile { it.copy(formImageUri = path) }
        _statusMessage.value = "Form image captured successfully!"
    }

    fun removeFormImage() {
        updateProfile { it.copy(formImageUri = null) }
    }

    fun resetForm() {
        _currentProfile.value = DossierProfile.createEmpty()
    }

    fun selectProfile(profile: DossierProfile) {
        _currentProfile.value = profile
    }

    fun saveCurrentProfile() {
        viewModelScope.launch {
            val profile = _currentProfile.value
            val id = repository.insertProfile(profile)
            _currentProfile.value = profile.copy(id = id)
            _statusMessage.value = "Profile saved successfully!"
        }
    }

    fun deleteProfile(profile: DossierProfile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            if (_currentProfile.value.id == profile.id) {
                resetForm()
            }
            _statusMessage.value = "Profile deleted"
        }
    }

    fun duplicateProfile(profile: DossierProfile) {
        viewModelScope.launch {
            val copy = profile.copy(
                id = 0,
                fullName = "${profile.fullName} (Copy)",
                updatedAt = System.currentTimeMillis()
            )
            repository.insertProfile(copy)
            _statusMessage.value = "Profile duplicated!"
        }
    }

    fun generatePdf(context: Context, onComplete: ((File) -> Unit)? = null) {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            try {
                val file = PdfGenerator.generatePdf(context, _currentProfile.value)
                _lastGeneratedPdf.value = file
                _statusMessage.value = "PDF generated successfully!"
                onComplete?.invoke(file)
            } catch (e: Exception) {
                _statusMessage.value = "Error generating PDF: ${e.message}"
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }

    fun saveToDownloads(context: Context) {
        viewModelScope.launch {
            val file = _lastGeneratedPdf.value ?: run {
                _isGeneratingPdf.value = true
                val newFile = try {
                    PdfGenerator.generatePdf(context, _currentProfile.value)
                } catch (e: Exception) {
                    _statusMessage.value = "Failed to generate PDF: ${e.message}"
                    _isGeneratingPdf.value = false
                    return@launch
                }
                _lastGeneratedPdf.value = newFile
                _isGeneratingPdf.value = false
                newFile
            }

            val displayName = "${_currentProfile.value.fullName.ifBlank { "Dossier" }}_Profile.pdf"
            val uri = PdfGenerator.savePdfToDownloads(context, file, displayName)
            if (uri != null) {
                _statusMessage.value = "Saved to Downloads folder!"
            } else {
                _statusMessage.value = "Failed to save to Downloads"
            }
        }
    }
}
