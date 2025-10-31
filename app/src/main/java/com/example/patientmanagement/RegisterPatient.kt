package com.example.patientmanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.patientmanagement.data.AppDatabase
import com.example.patientmanagement.model.Patient
import com.example.patientmanagement.workers.SyncManager
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

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_patient)

        // Initialize UI
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

        // Button listeners
        btnSave.setOnClickListener {
            if (validateForm()) savePatient()
        }

        btnClose.setOnClickListener { finish() }

        // Schedule periodic sync when app opens
        SyncManager.schedulePeriodicSync(applicationContext)
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
                Log.d("RegisterPatient", "💾 Patient saved locally: ${patient.firstName}")

                // Trigger one-time sync via SyncManager
                SyncManager.triggerImmediateSync(applicationContext)

                withContext(Dispatchers.Main) {
                    Snackbar.make(btnSave, "Patient saved locally! Sync queued.", Snackbar.LENGTH_LONG).show()
                    navigateToVitals(patient)
                }
            } catch (e: Exception) {
                Log.e("RegisterPatient", "❌ Error saving patient: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Snackbar.make(btnSave, "Error saving patient: ${e.message}", Snackbar.LENGTH_LONG).show()
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