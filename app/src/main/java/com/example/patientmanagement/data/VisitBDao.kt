package com.example.patientmanagement.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.patientmanagement.model.VisitA
import com.example.patientmanagement.model.VisitB
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitBDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitB(visitB: VisitB)

    @Query("SELECT * FROM visit_b WHERE patientId = :patientId ORDER BY visitDate DESC")
    fun getVisitsByPatient(patientId: String): Flow<List<VisitB>>

    @Query("SELECT * FROM visit_b WHERE synced = 0")
    suspend fun getUnsyncedVisitsB(): List<VisitB>

    @Query("UPDATE visit_b SET synced = 1 WHERE id = :id")
    suspend fun updateSyncStatus(id: Int)
}
