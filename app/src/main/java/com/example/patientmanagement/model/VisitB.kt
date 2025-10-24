package com.example.patientmanagement.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "visit_b")
data class VisitB(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: String,
    val visitDate: Date,
    val generalHealth: String,
    val usingDrugs: String,
    val comments: String
)
