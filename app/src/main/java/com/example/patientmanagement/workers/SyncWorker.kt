package com.example.patientmanagement.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.patientmanagement.data.AppDatabase
import com.example.patientmanagement.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val apiService: ApiService = ApiClient.retrofit.create(ApiService::class.java)
    private val db = AppDatabase.getDatabase(appContext)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // -----------------------
            // 1️⃣ SYNC UNSYNCED PATIENTS
            // -----------------------
            val unsyncedPatients = db.patientDao().getUnsyncedPatients()
            Log.d("SyncWorker", "Found ${unsyncedPatients.size} unsynced patients")

            for (patient in unsyncedPatients) {
                val networkPatient = Patient(
                    patient_id = patient.patientId,
                    registration_date = dateFormat.format(patient.registrationDate),
                    first_name = patient.firstName,
                    last_name = patient.lastName,
                    date_of_birth = dateFormat.format(patient.dateOfBirth),
                    gender = patient.gender
                )

                try {
                    val response = apiService.registerPatient(networkPatient)
                    when {
                        response.isSuccessful -> {
                            db.patientDao().insertPatient(patient.copy(synced = true))
                            Log.d("SyncWorker", "✅ Synced patient ${patient.patientId}")
                        }

                        response.code() == 400 &&
                                response.errorBody()?.string()
                                    ?.contains("patient with this patient id already exists", true) == true -> {
                            db.patientDao().insertPatient(patient.copy(synced = true))
                            Log.w("SyncWorker", "⚠️ Duplicate patient ${patient.patientId} marked as synced.")
                        }

                        else -> {
                            val err = response.errorBody()?.string()
                            Log.e("SyncWorker", "❌ Failed to sync ${patient.patientId}: ${response.code()} - $err")
                        }
                    }
                } catch (e: IOException) {
                    Log.e("SyncWorker", "🌐 Network error for ${patient.patientId}: ${e.message}")
                    return@withContext Result.retry()
                } catch (e: HttpException) {
                    Log.e("SyncWorker", "⚠️ HTTP error for ${patient.patientId}: ${e.message}")
                }
            }

            // -----------------------
            // 2️⃣ SYNC UNSYNCED VITALS
            // -----------------------
            val unsyncedVitals = db.vitalsDao().getUnsyncedVitals()
            Log.d("SyncWorker", "Found ${unsyncedVitals.size} unsynced vitals")

            for (vitals in unsyncedVitals) {
                try {
                    val networkVitals = Vitals(
                        patient = vitals.patientId,
                        visit_date = dateFormat.format(vitals.visitDate),
                        height_cm = vitals.heightCm,
                        weight_kg = vitals.weightKg,
                        bmi = vitals.bmi
                    )

                    val response = apiService.submitVitals(networkVitals)
                    if (response.isSuccessful) {
                        db.vitalsDao().updateSyncStatus(vitals.id)
                        Log.d("SyncWorker", "✅ Synced vitals for ${vitals.patientId}")
                    } else {
                        val err = response.errorBody()?.string()
                        Log.e("SyncWorker", "❌ Failed to sync vitals for ${vitals.patientId}: ${response.code()} - $err")
                    }
                } catch (e: IOException) {
                    Log.e("SyncWorker", "🌐 Network error for vitals of ${vitals.patientId}: ${e.message}")
                    return@withContext Result.retry()
                } catch (e: HttpException) {
                    Log.e("SyncWorker", "⚠️ HTTP error for vitals of ${vitals.patientId}: ${e.message}")
                }
            }

            // -----------------------
            // 3️⃣ SYNC UNSYNCED VISIT FORM A
            // -----------------------
            val unsyncedVisitA = db.visitADao().getUnsyncedVisitsA()
            Log.d("SyncWorker", "Found ${unsyncedVisitA.size} unsynced VisitFormA records")

            for (visitA in unsyncedVisitA) {
                try {
                    val networkVisitA = VisitA(
                        patient = visitA.patientId,
                        visit_date = dateFormat.format(visitA.visitDate),
                        general_health = visitA.generalHealth,
                        on_diet = visitA.onDiet,
                        comments = visitA.comments
                    )

                    val response = apiService.submitVisitFormA(networkVisitA)
                    if (response.isSuccessful) {
                        db.visitADao().updateSyncStatus(visitA.id)
                        Log.d("SyncWorker", "✅ Synced VisitFormA for ${visitA.patientId}")
                    } else {
                        val err = response.errorBody()?.string()
                        Log.e("SyncWorker", "❌ Failed to sync VisitFormA for ${visitA.patientId}: ${response.code()} - $err")
                    }
                } catch (e: Exception) {
                    Log.e("SyncWorker", "⚠️ Error syncing VisitFormA for ${visitA.patientId}: ${e.message}")
                }
            }

            // -----------------------
            // 4️⃣ SYNC UNSYNCED VISIT FORM B
            // -----------------------
            val unsyncedVisitB = db.visitBDao().getUnsyncedVisitsB()
            Log.d("SyncWorker", "Found ${unsyncedVisitB.size} unsynced VisitFormB records")

            for (visitB in unsyncedVisitB) {
                try {
                    val networkVisitB = VisitB(
                        patient = visitB.patientId,
                        visit_date = dateFormat.format(visitB.visitDate),
                        general_health = visitB.generalHealth,
                        using_drugs = visitB.usingDrugs,
                        comments = visitB.comments
                    )

                    val response = apiService.submitVisitFormB(networkVisitB)
                    if (response.isSuccessful) {
                        db.visitBDao().updateSyncStatus(visitB.id)
                        Log.d("SyncWorker", "✅ Synced VisitFormB for ${visitB.patientId}")
                    } else {
                        val err = response.errorBody()?.string()
                        Log.e("SyncWorker", "❌ Failed to sync VisitFormB for ${visitB.patientId}: ${response.code()} - $err")
                    }
                } catch (e: Exception) {
                    Log.e("SyncWorker", "⚠️ Error syncing VisitFormB for ${visitB.patientId}: ${e.message}")
                }
            }

            // ✅ Done
            Result.success()

        } catch (e: Exception) {
            Log.e("SyncWorker", "💥 Unexpected error: ${e.message}", e)
            Result.retry()
        }
    }
}
