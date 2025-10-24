package com.example.patientmanagement.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.patientmanagement.model.VisitA
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitADao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitA(visitA: VisitA)

    @Query("SELECT * FROM visit_a WHERE patientId = :patientId ORDER BY visitDate DESC")
    fun getVisitsByPatient(patientId: String): Flow<List<VisitA>>
}
