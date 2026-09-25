package com.example.data.repository

import com.example.data.local.DossierDao
import com.example.data.model.DossierProfile
import kotlinx.coroutines.flow.Flow

class DossierRepository(private val dossierDao: DossierDao) {
    val allProfiles: Flow<List<DossierProfile>> = dossierDao.getAllProfiles()

    suspend fun getProfileById(id: Long): DossierProfile? {
        return dossierDao.getProfileById(id)
    }

    suspend fun insertProfile(profile: DossierProfile): Long {
        return dossierDao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: DossierProfile) {
        dossierDao.updateProfile(profile)
    }

    suspend fun deleteProfile(profile: DossierProfile) {
        dossierDao.deleteProfile(profile)
    }

    suspend fun deleteProfileById(id: Long) {
        dossierDao.deleteProfileById(id)
    }

    suspend fun getCount(): Int {
        return dossierDao.getCount()
    }
}
