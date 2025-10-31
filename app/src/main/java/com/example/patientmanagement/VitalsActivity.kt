package com.example.patientmanagement

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.patientmanagement.data.AppDatabase
import com.example.patientmanagement.model.Patient
import com.example.patientmanagement.model.Vitals
import com.example.patientmanagement.workers.SyncManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class VitalsActivity : AppCompatActivity() {

    private lateinit var etPatientName: MaterialAutoCompleteTextView
    private lateinit var etVisitDate: TextInputEditText
    private lateinit var etHeight: TextInputEditText
    private lateinit var etWeight: TextInputEditText
    private lateinit var etBmi: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnClose: MaterialButton

    private var selectedPatientId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vitals)

        // Initialize fields
        etPatientName = findViewById(R.id.etPatientName)
        etVisitDate = findViewById(R.id.etVisitDate)
        etHeight = findViewById(R.id.etHeight)
        etWeight = findViewById(R.id.etWeight)
        etBmi = findViewById(R.id.etBmi)
        btnSave = findViewById(R.id.btnSave)
        btnClose = findViewById(R.id.btnClose)

        val db = AppDatabase.getDatabase(applicationContext)

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dateConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .build()

        // Step 1: Load patients into dropdown
        // Check if a patientId was passed from a previous activity
        val patientIdFromIntent = intent.getStringExtra("patientId")
        selectedPatientId = patientIdFromIntent

        if (patientIdFromIntent != null) {
            // A specific patient was passed, so load their details and disable the dropdown.
            etPatientName.isEnabled = false // Disable the TextInputLayout to prevent changes
            lifecycleScope.launch(Dispatchers.IO) {
                val patient = db.patientDao().getPatientById(patientIdFromIntent)
                withContext(Dispatchers.Main) {
                    patient?.let {
                        etPatientName.setText("${it.firstName} ${it.lastName}", false) // Set text without filtering
                    }
                }
            }
        } else {
            // No specific patient was passed, so enable the dropdown and load all patients.
            etPatientName.isEnabled = true
            lifecycleScope.launch(Dispatchers.IO) {
                db.patientDao().getAllPatients().collectLatest { patients ->
                    withContext(Dispatchers.Main) {
                        setupPatientDropdown(patients)
                    }
                }
            }
        }

        etVisitDate.setText(dateFormat.format(Date()))

        // Step 2: Visit Date Picker
        etVisitDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Visit Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(dateConstraints)
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val date = Date(selection)
                etVisitDate.setText(dateFormat.format(date))
            }

            datePicker.show(supportFragmentManager, "visit_date_picker")
        }

        // Step 3: Auto-calculate BMI
        etHeight.addTextChangedListener { calculateBmi() }
        etWeight.addTextChangedListener { calculateBmi() }

        // Step 4: Save Vitals
        btnSave.setOnClickListener {
            if (validateForm()) {
                val heightCm = etHeight.text.toString().toDouble()
                val weightKg = etWeight.text.toString().toDouble()
                val heightM = heightCm / 100
                val bmi = weightKg / (heightM * heightM)

                val vital = Vitals(
                    patientId = selectedPatientId ?: "",
                    visitDate = dateFormat.parse(etVisitDate.text.toString())!!,
                    heightCm = heightCm,
                    weightKg = weightKg,
                    bmi = bmi
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    db.vitalsDao().insertVitals(vital)

                    // Trigger one-time sync via SyncManager
                    SyncManager.triggerImmediateSync(applicationContext)

                    withContext(Dispatchers.Main) {
                        Snackbar.make(btnSave, "Vitals saved successfully!", Snackbar.LENGTH_LONG).show()
                        val nextIntent = if (bmi < 25) {
                            Intent(this@VitalsActivity, VisitPageAActivity::class.java)
                        } else {
                            Intent(this@VitalsActivity, VisitPageBActivity::class.java)
                        }

                        nextIntent.putExtra("patientId", selectedPatientId)
                        startActivity(nextIntent)
                        finish()
                        finish()
                    }
                }
            }
        }

        // 🔹 Step 5: Close button
        btnClose.setOnClickListener { finish() }
    }

    private fun setupPatientDropdown(patients: List<Patient>) {
        val patientNames = patients.map { "${it.firstName} ${it.lastName}" }
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, patientNames)
        etPatientName.setAdapter(adapter)

        etPatientName.setOnItemClickListener { _, _, position, _ ->
            val selectedPatient = patients[position]
            selectedPatientId = selectedPatient.patientId
        }
    }

    private fun validateForm(): Boolean {
        val fields = listOf(etPatientName, etVisitDate, etHeight, etWeight)
        for (field in fields) {
            if (field.text.isNullOrBlank()) {
                field.error = "Required"
                return false
            }
        }
        if (selectedPatientId == null) {
            etPatientName.error = "Please select a valid patient"
            return false
        }
        return true
    }

    private fun calculateBmi() {
        val heightText = etHeight.text.toString()
        val weightText = etWeight.text.toString()

        if (heightText.isNotEmpty() && weightText.isNotEmpty()) {
            val heightCm = heightText.toDoubleOrNull()
            val weightKg = weightText.toDoubleOrNull()

            if (heightCm != null && weightKg != null && heightCm > 0) {
                val bmi = weightKg / Math.pow(heightCm / 100, 2.0)
                etBmi.setText(String.format("%.1f", bmi))
            }
        }
    }
}
