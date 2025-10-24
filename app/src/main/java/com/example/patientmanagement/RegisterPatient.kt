package com.example.patientmanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.patientmanagement.data.AppDatabase
import com.example.patientmanagement.model.Patient
import com.example.patientmanagement.network.ApiClient
import com.example.patientmanagement.network.ApiService
import com.example.patientmanagement.network.Patient as NetworkPatient
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RegisterPatient : AppCompatActivity() {

    private lateinit var etPatientId: TextInputEditText
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etRegistrationDate: TextInputEditText
    private lateinit var etDob: TextInputEditText
    private lateinit var etGender: MaterialAutoCompleteTextView
    private lateinit var btnSave: MaterialButton
    private lateinit var btnClose: MaterialButton

    private val apiService by lazy { ApiClient.retrofit.create(ApiService::class.java) }
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_patient)

        // Initialize fields
        etPatientId = findViewById(R.id.etPatientId)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etRegistrationDate = findViewById(R.id.etRegistrationDate)
        etDob = findViewById(R.id.etDob)
        etGender = findViewById(R.id.etGender)
        btnSave = findViewById(R.id.btnSave)
        btnClose = findViewById(R.id.btnClose)

        // Gender dropdown
        val genderOptions = listOf("Male", "Female", "Other")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderOptions)
        etGender.setAdapter(adapter)

        // Prefill registration date
        etRegistrationDate.setText(dateFormat.format(Date()))

        val dateConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .build()

        // Date pickers
        etRegistrationDate.setOnClickListener { showDatePicker(it as TextInputEditText, "Select Registration Date", dateConstraints) }
        etDob.setOnClickListener { showDatePicker(it as TextInputEditText, "Select Date of Birth", dateConstraints) }

        btnSave.setOnClickListener {
            if (validateForm()) savePatient()
        }

        btnClose.setOnClickListener { finish() }
    }

    private fun showDatePicker(target: TextInputEditText, title: String, constraints: CalendarConstraints) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setCalendarConstraints(constraints)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val date = Date(selection)
            target.setText(dateFormat.format(date))
        }

        datePicker.show(supportFragmentManager, title)
    }

    private fun savePatient() {
        val patient = Patient(
            patientId = etPatientId.text.toString(),
            firstName = etFirstName.text.toString(),
            lastName = etLastName.text.toString(),
            registrationDate = dateFormat.parse(etRegistrationDate.text.toString())!!,
            dateOfBirth = dateFormat.parse(etDob.text.toString())!!,
            gender = etGender.text.toString(),
            synced = false
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                db.patientDao().insertPatient(patient)

                // ✅ Log when saving locally
                Log.d("SYNC", "Patient saved locally: ${patient.firstName}")

                // Prepare network object
                val networkDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val networkPatient = NetworkPatient(
                    patient_id = patient.patientId,
                    registration_date = networkDateFormat.format(patient.registrationDate),
                    first_name = patient.firstName,
                    last_name = patient.lastName,
                    date_of_birth = networkDateFormat.format(patient.dateOfBirth),
                    gender = patient.gender
                )

                Log.d("SYNC", "Attempting to sync to server...")

                try {
                    val response = apiService.registerPatient(networkPatient)
                    if (response.isSuccessful) {
                        db.patientDao().insertPatient(patient.copy(synced = true))
                        Log.d("SYNC", "Patient synced successfully with server.")
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("SYNC", "Server error: ${response.code()} - ${response.message()} - $errorBody")
                    }
                } catch (e: IOException) {
                    Log.e("SYNC", "Network error: ${e.message}")
                } catch (e: HttpException) {
                    Log.e("SYNC", "HTTP error: ${e.message}")
                }

                withContext(Dispatchers.Main) {
                    Log.d("SYNC", "Showing snackbar and navigating to vitals")
                    Snackbar.make(btnSave, "Patient saved successfully!", Snackbar.LENGTH_LONG).show()
                    navigateToVitals(patient)
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Unexpected crash: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Snackbar.make(btnSave, "An error occurred: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun navigateToVitals(patient: Patient) {
        val intent = Intent(this, VitalsActivity::class.java).apply {
            putExtra("patientId", patient.patientId)
            putExtra("patientName", "${patient.firstName} ${patient.lastName}")
        }
        startActivity(intent)
        finish()
    }

    private fun validateForm(): Boolean {
        val fields = listOf(etPatientId, etFirstName, etLastName, etRegistrationDate, etDob)
        for (f in fields) {
            if (f.text.isNullOrBlank()) {
                f.error = "Required"
                return false
            }
        }

        if (etGender.text.isNullOrBlank()) {
            etGender.error = "Select gender"
            return false
        }

        return true
    }
}
