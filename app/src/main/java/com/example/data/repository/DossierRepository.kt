package com.example.data.repository

import com.example.data.local.DossierDao
import com.example.data.model.DossierProfile
import kotlinx.coroutines.flow.Flow

class DossierRepository(private val dao: DossierDao) {
    fun getAllProfiles(): Flow<List<DossierProfile>> = dao.getAllProfiles()

    suspend fun getProfileById(id: Long): DossierProfile? = dao.getProfileById(id)

    suspend fun getCount(): Int = dao.getCount()

    suspend fun insertProfile(profile: DossierProfile): Long = dao.insertProfile(profile)

    suspend fun updateProfile(profile: DossierProfile) = dao.updateProfile(profile)

    suspend fun deleteProfile(profile: DossierProfile) = dao.deleteProfile(profile)

    suspend fun deleteProfileById(id: Long) = dao.deleteProfileById(id)
}
