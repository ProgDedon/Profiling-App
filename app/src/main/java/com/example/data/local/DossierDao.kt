package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DossierProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface DossierDao {
    @Query("SELECT * FROM dossier_profiles ORDER BY updatedAt DESC")
    fun getAllProfiles(): Flow<List<DossierProfile>>

    @Query("SELECT * FROM dossier_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): DossierProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: DossierProfile): Long

    @Update
    suspend fun updateProfile(profile: DossierProfile)

    @Delete
    suspend fun deleteProfile(profile: DossierProfile)

    @Query("DELETE FROM dossier_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long)

    @Query("SELECT COUNT(*) FROM dossier_profiles")
    suspend fun getCount(): Int
}
