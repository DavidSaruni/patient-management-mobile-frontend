package com.example.patientmanagement.utils

import android.content.Context
import com.example.patientmanagement.data.AppDatabase
import com.example.patientmanagement.model.Patient
import com.example.patientmanagement.model.Vitals
import com.example.patientmanagement.model.VisitA
import com.example.patientmanagement.model.VisitB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.util.*

object SampleDataGenerator {
    
    fun generateSampleData(context: Context) {
        val database = AppDatabase.getDatabase(context)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if sample data already exists
                val existingPatients = database.patientDao().getAllPatients().first()
                if (existingPatients.isEmpty()) {
                    insertSampleData(database)
                }
            } catch (e: Exception) {
                // If there's an error, still try to insert sample data
                insertSampleData(database)
            }
        }
    }
    
    private suspend fun insertSampleData(database: AppDatabase) {
        val calendar = Calendar.getInstance()
        
        // Sample patients
        val patients = listOf(
            Patient(
                patientId = "P001",
                firstName = "Hope",
                lastName = "Shapash",
                registrationDate = Date(),
                dateOfBirth = Calendar.getInstance().apply { 
                    set(2012, Calendar.JANUARY, 15) 
                }.time,
                gender = "Female",
                synced = true
            ),
            Patient(
                patientId = "P002",
                firstName = "Justine",
                lastName = "Nabs",
                registrationDate = Date(),
                dateOfBirth = Calendar.getInstance().apply { 
                    set(2019, Calendar.MARCH, 22) 
                }.time,
                gender = "Male",
                synced = true
            ),
            Patient(
                patientId = "P003",
                firstName = "Timz",
                lastName = "Owen",
                registrationDate = Date(),
                dateOfBirth = Calendar.getInstance().apply { 
                    set(2024, Calendar.JUNE, 10) 
                }.time,
                gender = "Male",
                synced = true
            ),
            Patient(
                patientId = "P004",
                firstName = "Sarah",
                lastName = "Johnson",
                registrationDate = Date(),
                dateOfBirth = Calendar.getInstance().apply { 
                    set(2010, Calendar.SEPTEMBER, 5) 
                }.time,
                gender = "Female",
                synced = true
            )
        )
        
        // Insert patients
        patients.forEach { patient ->
            database.patientDao().insertPatient(patient)
        }
        
        // Sample vitals
        val vitals = listOf(
            Vitals(
                patientId = "P001",
                visitDate = Date(),
                heightCm = 150.0,
                weightKg = 50.0,
                bmi = 22.2, // Normal BMI - will show Visit A
                synced = true
            ),
            Vitals(
                patientId = "P002",
                visitDate = Date(),
                heightCm = 110.0,
                weightKg = 30.0,
                bmi = 24.8, // Normal BMI - will show Visit A
                synced = true
            ),
            Vitals(
                patientId = "P003",
                visitDate = Date(),
                heightCm = 60.0,
                weightKg = 10.0,
                bmi = 27.8, // Overweight BMI - will show Visit B
                synced = true
            ),
            Vitals(
                patientId = "P004",
                visitDate = Date(),
                heightCm = 160.0,
                weightKg = 64.0,
                bmi = 25.0, // Exactly BMI 25 - will show Visit B
                synced = true
            )
        )
        
        // Insert vitals
        vitals.forEach { vital ->
            database.vitalsDao().insertVitals(vital)
        }
        
        // Sample visits
        val visitsA = listOf(
            VisitA(
                patientId = "P001",
                visitDate = Date(),
                generalHealth = "Good",
                onDiet = "Yes",
                comments = "Patient is following diet plan well",
                synced = true
            ),
            VisitA(
                patientId = "P002",
                visitDate = Date(),
                generalHealth = "Fair",
                onDiet = "No",
                comments = "Needs to improve diet habits",
                synced = true
            )
        )
        
        val visitsB = listOf(
            VisitB(
                patientId = "P001",
                visitDate = Date(),
                generalHealth = "Good",
                usingDrugs = "No",
                comments = "No medications required",
                synced = true
            ),
            VisitB(
                patientId = "P003",
                visitDate = Date(),
                generalHealth = "Poor",
                usingDrugs = "Yes",
                comments = "On prescribed medication",
                synced = true
            ),
            VisitB(
                patientId = "P004",
                visitDate = Date(),
                generalHealth = "Fair",
                usingDrugs = "No",
                comments = "Monitoring weight management",
                synced = true
            )
        )
        
        // Insert visits
        visitsA.forEach { visit ->
            database.visitADao().insertVisitA(visit)
        }
        
        visitsB.forEach { visit ->
            database.visitBDao().insertVisitB(visit)
        }
    }
}

