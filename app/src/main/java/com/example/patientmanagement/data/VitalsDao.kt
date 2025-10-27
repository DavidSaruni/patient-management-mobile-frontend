package com.example.patientmanagement.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.patientmanagement.model.Vitals
import kotlinx.coroutines.flow.Flow

@Dao
interface VitalsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitals(vitals: Vitals)

    // Get all vitals for a specific patient
    @Query("SELECT * FROM vitals WHERE patientId = :patientId ORDER BY visitDate DESC")
    fun getVitalsForPatient(patientId: String): Flow<List<Vitals>>

    // ✅ Get the latest vitals record for a specific patient (used in MainActivity)
    @Query("""
        SELECT * FROM vitals 
        WHERE patientId = :patientId 
        ORDER BY visitDate DESC 
        LIMIT 1
    """)
    suspend fun getLastVitalsByPatientId(patientId: String): Vitals?

    // Get all vitals for a given date
    @Query("""
        SELECT * FROM vitals 
        WHERE date(visitDate / 1000, 'unixepoch') = date(:selectedDate / 1000, 'unixepoch')
    """)
    suspend fun getVitalsByDate(selectedDate: Long): List<Vitals>

    // 🔹 Get unsynced vitals for background sync
    @Query("SELECT * FROM vitals WHERE synced = 0")
    suspend fun getUnsyncedVitals(): List<Vitals>

    // 🔹 Mark vitals as synced
    @Query("UPDATE vitals SET synced = 1 WHERE id = :id")
    suspend fun updateSyncStatus(id: Int)
}
