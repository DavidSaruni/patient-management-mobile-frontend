package com.example.patientmanagement

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.patientmanagement.data.AppDatabase
import com.example.patientmanagement.model.Patient
import com.example.patientmanagement.model.VisitA
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

class VisitPageAActivity : AppCompatActivity() {

    private lateinit var etPatientName: MaterialAutoCompleteTextView
    private lateinit var etVisitDate: TextInputEditText
    private lateinit var etComments: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnClose: MaterialButton

    private lateinit var db: AppDatabase

    private var selectedPatientId: String? = null
    private var selectedHealth: String? = null
    private var selectedDiet: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_page_a)

        db = AppDatabase.getDatabase(applicationContext)

        // Initialize views
        etPatientName = findViewById(R.id.etPatientName)
        etVisitDate = findViewById(R.id.etVisitDate)
        etComments = findViewById(R.id.etComments)
        btnSave = findViewById(R.id.btnSave)
        btnClose = findViewById(R.id.btnClose)

        val rgGeneralHealth = findViewById<android.widget.RadioGroup>(R.id.rgGeneralHealth)
        val rgOnDiet = findViewById<android.widget.RadioGroup>(R.id.rgOnDiet)

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        // 🔹 Load patient dropdown
        lifecycleScope.launch(Dispatchers.IO) {
            db.patientDao().getAllPatients().collectLatest { patients ->
                withContext(Dispatchers.Main) {
                    setupPatientDropdown(patients)
                }
            }
        }

        // 🔹 Visit Date Picker
        val dateConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .build()

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

        // 🔹 Radio selections
        rgGeneralHealth.setOnCheckedChangeListener { _, checkedId ->
            selectedHealth = when (checkedId) {
                R.id.rbHealthGood -> "Good"
                R.id.rbHealthPoor -> "Poor"
                else -> null
            }
        }

        rgOnDiet.setOnCheckedChangeListener { _, checkedId ->
            selectedDiet = when (checkedId) {
                R.id.rbDietYes -> "Yes"
                R.id.rbDietNo -> "No"
                else -> null
            }
        }

        // 🔹 Save Button
        btnSave.setOnClickListener {
            if (validateForm()) {
                val visitDate = dateFormat.parse(etVisitDate.text.toString())!!
                val visitA = VisitA(
                    patientId = selectedPatientId ?: "",
                    visitDate = visitDate,
                    generalHealth = selectedHealth!!,
                    onDiet = selectedDiet!!,
                    comments = etComments.text.toString().trim()
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    db.visitADao().insertVisitA(visitA)
                    withContext(Dispatchers.Main) {
                        Snackbar.make(btnSave, "Visit Form A saved successfully!", Snackbar.LENGTH_LONG).show()
                        finish() // ✅ return to patient listing
                    }
                }
            }
        }

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
        var valid = true

        if (selectedPatientId == null) {
            etPatientName.error = "Please select a patient"
            valid = false
        }

        if (etVisitDate.text.isNullOrBlank()) {
            etVisitDate.error = "Select a date"
            valid = false
        }

        if (selectedHealth == null) {
            Snackbar.make(btnSave, "Select General Health", Snackbar.LENGTH_SHORT).show()
            valid = false
        }

        if (selectedDiet == null) {
            Snackbar.make(btnSave, "Select Diet option", Snackbar.LENGTH_SHORT).show()
            valid = false
        }

        if (etComments.text.isNullOrBlank()) {
            etComments.error = "Comments are required"
            valid = false
        }

        return valid
    }
}
