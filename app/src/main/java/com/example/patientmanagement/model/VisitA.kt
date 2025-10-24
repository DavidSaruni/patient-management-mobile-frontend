package com.example.patientmanagement.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "visit_a")
data class VisitA(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: String,
    val visitDate: Date,
    val generalHealth: String,
    val onDiet: String,
    val comments: String
)
