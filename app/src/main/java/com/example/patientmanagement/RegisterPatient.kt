package com.example.patientmanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.patientmanagement.data.AppDatabase
import com.example.patientmanagement.model.Patient
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
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

        // Gender Dropdown
        val genderOptions = listOf("Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderOptions)
        etGender.setAdapter(genderAdapter)
        etGender.setOnClickListener { etGender.showDropDown() }

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        // Prefill Registration Date with today's date
        val today = Date()
        etRegistrationDate.setText(dateFormat.format(today))

        // Restrict date selection to past or current dates only
        val dateConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .build()

        // Registration Date Picker
        etRegistrationDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Registration Date")
                .setCalendarConstraints(dateConstraints)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val date = Date(selection)
                etRegistrationDate.setText(dateFormat.format(date))
            }

            datePicker.show(supportFragmentManager, "registration_date_picker")
        }

        // Date of Birth Picker
        etDob.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date of Birth")
                .setCalendarConstraints(dateConstraints)
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val date = Date(selection)
                etDob.setText(dateFormat.format(date))
            }

            datePicker.show(supportFragmentManager, "dob_date_picker")
        }

        // Save Button Logic
        btnSave.setOnClickListener {
            if (validateForm()) {
                val patient = Patient(
                    patientId = etPatientId.text.toString(),
                    firstName = etFirstName.text.toString(),
                    lastName = etLastName.text.toString(),
                    registrationDate = dateFormat.parse(etRegistrationDate.text.toString())!!,
                    dateOfBirth = dateFormat.parse(etDob.text.toString())!!,
                    gender = etGender.text.toString()
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(applicationContext)
                    db.patientDao().insertPatient(patient)

                    withContext(Dispatchers.Main) {
                        Snackbar.make(btnSave, "Patient saved successfully!", Snackbar.LENGTH_LONG).show()
                        Log.d("RegisterPatient", "Saved: ${patient.firstName} ${patient.lastName}")
                        // ✅ Redirect to VitalsActivity after successful save
                        val intent = Intent(this@RegisterPatient, VitalsActivity::class.java).apply {
                            putExtra("patientId", patient.patientId)
                            putExtra("patientName", "${patient.firstName} ${patient.lastName}")
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }


        // Close Button Logic
        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun validateForm(): Boolean {
        val fields = listOf(
            etPatientId,
            etFirstName,
            etLastName,
            etRegistrationDate,
            etDob,
        )

        for (field in fields) {
            if (field.text.isNullOrBlank()) {
                field.error = "Required"
                return false
            }
        }

        if (etGender.text.isNullOrBlank()) {
            etGender.error = "Select Gender"
            return false
        }

        return true
    }

    private fun clearFields() {
        etPatientId.text?.clear()
        etFirstName.text?.clear()
        etLastName.text?.clear()
        etDob.text?.clear()
        etGender.text?.clear()

        val today = Date()
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        etRegistrationDate.setText(dateFormat.format(today))
    }
}
