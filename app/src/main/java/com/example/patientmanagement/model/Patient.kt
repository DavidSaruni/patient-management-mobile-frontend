package com.example.patientmanagement.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey val patientId: String, // Unique field
    val firstName: String,
    val lastName: String,
    val registrationDate: Date,
    val dateOfBirth: Date,
    val gender: String,
    val synced: Boolean = false
)
