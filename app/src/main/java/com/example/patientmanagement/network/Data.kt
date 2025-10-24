package com.example.patientmanagement.network

data class Patient(
    val patient_id: String,
    val registration_date: String? = null,
    val first_name: String,
    val last_name: String,
    val date_of_birth: String,
    val gender: String
)

data class Vitals(
    val patient: String, // patient_id or numeric ID (check your backend)
    val visit_date: String,
    val height_cm: Double,
    val weight_kg: Double,
    val bmi: Double
)

data class VisitA(
    val patient: String,
    val visit_date: String,
    val general_health: String,
    val on_diet: Boolean,
    val comments: String
)

data class VisitB(
    val patient: String,
    val visit_date: String,
    val general_health: String,
    val using_drugs: Boolean,
    val comments: String
)
