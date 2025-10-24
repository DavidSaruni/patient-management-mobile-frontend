package com.example.patientmanagement.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "vitals")
data class Vitals(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: String,
    val visitDate: Date,
    val heightCm: Double,
    val weightKg: Double,
    val bmi: Double,
    val synced: Boolean = false
)
