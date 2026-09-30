package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class CustomField(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "",
    val value: String = ""
)

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
    val twitter: String = "",
    val instagram: String = "",
    val youtube: String = "",
    val otherSocialMedia: String = "",
    val occupation: String = "",
    val education: String = "",
    val associatesPhoneNumbers: String = "",
    val formImageUri: String? = null,
    val personalInfoJson: String = "[]",
    val customFieldsJson: String = "[]",
    val watermarkText: String = "MMC",
    val headerColorHex: String = "#102E56",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val displayPhotoNumber: String
        get() = if (phoneNumber.isNotBlank()) phoneNumber else photoDisplayNumber

    fun getPersonalInfoList(): List<CustomField> = parseJsonFields(personalInfoJson)
    fun withPersonalInfoList(fields: List<CustomField>): DossierProfile =
        this.copy(personalInfoJson = toJsonString(fields))

    fun getOtherInfoList(): List<CustomField> = parseJsonFields(customFieldsJson)
    fun withOtherInfoList(fields: List<CustomField>): DossierProfile =
        this.copy(customFieldsJson = toJsonString(fields))

    fun getCustomFields(): List<CustomField> = parseJsonFields(customFieldsJson)
    fun withCustomFields(fields: List<CustomField>): DossierProfile =
        this.copy(customFieldsJson = toJsonString(fields))

    private fun parseJsonFields(json: String): List<CustomField> {
        if (json.isBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<CustomField>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CustomField(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        label = obj.optString("label", ""),
                        value = obj.optString("value", "")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun toJsonString(fields: List<CustomField>): String {
        val array = JSONArray()
        for (f in fields) {
            val obj = JSONObject()
            obj.put("id", f.id)
            obj.put("label", f.label)
            obj.put("value", f.value)
            array.put(obj)
        }
        return array.toString()
    }

    companion object {
        fun createEmpty(): DossierProfile = DossierProfile()
    }
}
