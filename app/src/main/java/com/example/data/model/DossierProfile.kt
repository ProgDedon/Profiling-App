package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dossier_profiles")
data class DossierProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String = "",
    val photoUri: String? = null,
    val photoDisplayNumber: String = "",
    val gender: String = "Male",
    val lgaState: String = "",
    val phoneNumber: String = "",
    val alsoKnownAs: String = "",
    val dateOfBirth: String = "",
    val facebook: String = "",
    val otherSocialMedia: String = "",
    val occupation: String = "",
    val education: String = "",
    val associatesPhoneNumbers: String = "",
    val watermarkText: String = "NS",
    val headerColorHex: String = "#102E56",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val displayPhotoNumber: String
        get() = if (photoDisplayNumber.isNotBlank()) photoDisplayNumber else phoneNumber

    companion object {
        fun createSample(): DossierProfile = DossierProfile(
            fullName = "MUBARAK ADULLAHI",
            photoUri = null,
            photoDisplayNumber = "07062270031",
            gender = "Male",
            lgaState = "Kontagori, Niger State",
            phoneNumber = "07062270031",
            alsoKnownAs = "Sardaunan Samari",
            dateOfBirth = "18th August, 1992",
            facebook = "Mubarak.abdullahi.739",
            otherSocialMedia = "",
            occupation = "Activist/ Advocate / ADC Supporter",
            education = "Government Secondary School Kontagora, Niger State",
            associatesPhoneNumbers = "08109595044 (Yusuf ALIYU)\n07037882149 (Musa Reskona ABUBAKAR)",
            watermarkText = "NS",
            headerColorHex = "#102E56"
        )
    }
}
