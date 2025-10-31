package com.example.patientmanagement

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.patientmanagement.data.AppDatabase
import com.example.patientmanagement.model.Patient
import com.example.patientmanagement.model.VisitB
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

class VisitPageBActivity : AppCompatActivity() {

    private lateinit var etPatientName: MaterialAutoCompleteTextView
    private lateinit var etVisitDate: TextInputEditText
    private lateinit var etComments: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnClose: MaterialButton

    private lateinit var db: AppDatabase
    private var selectedPatientId: String? = null
    private var selectedHealth: String? = null
    private var selectedDrugs: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_page_b)

        db = AppDatabase.getDatabase(applicationContext)

        // Initialize views
        etPatientName = findViewById(R.id.etPatientName)
        etVisitDate = findViewById(R.id.etVisitDate)
        etComments = findViewById(R.id.etComments)
        btnSave = findViewById(R.id.btnSave)
        btnClose = findViewById(R.id.btnClose)

        val rgGeneralHealth = findViewById<android.widget.RadioGroup>(R.id.rgGeneralHealth)
        val rgOnDrugs = findViewById<android.widget.RadioGroup>(R.id.rgOnDrugs)

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        // Load patients into dropdown
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

        // Visit Date Picker
        val dateConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .build()

        etVisitDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Visit Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(dateConstraints)
                .build()

            picker.addOnPositiveButtonClickListener {
                val date = Date(it)
                etVisitDate.setText(dateFormat.format(date))
            }

            picker.show(supportFragmentManager, "visit_date_picker")
        }

        // Radio selections
        rgGeneralHealth.setOnCheckedChangeListener { _, checkedId ->
            selectedHealth = when (checkedId) {
                R.id.rbHealthGood -> "Good"
                R.id.rbHealthPoor -> "Poor"
                else -> null
            }
        }

        rgOnDrugs.setOnCheckedChangeListener { _, checkedId ->
            selectedDrugs = when (checkedId) {
                R.id.rbDrugsYes -> "Yes"
                R.id.rbDrugsNo -> "No"
                else -> null
            }
        }

        // Save Button
        btnSave.setOnClickListener {
            if (validateForm()) {
                val visitDate = dateFormat.parse(etVisitDate.text.toString())!!
                val visitB = VisitB(
                    patientId = selectedPatientId ?: "",
                    visitDate = visitDate,
                    generalHealth = selectedHealth!!,
                    usingDrugs = selectedDrugs!!,
                    comments = etComments.text.toString().trim()
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    db.visitBDao().insertVisitB(visitB)

                    // Trigger one-time sync via SyncManager
                    SyncManager.triggerImmediateSync(applicationContext)

                    withContext(Dispatchers.Main) {
                        Snackbar.make(btnSave, "Visit B saved successfully!", Snackbar.LENGTH_LONG).show()
                        finish() // Return to patient list
                    }
                }
            }
        }

        btnClose.setOnClickListener { finish() }
    }

    private fun setupPatientDropdown(patients: List<Patient>) {
        val names = patients.map { "${it.firstName} ${it.lastName}" }
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
        etPatientName.setAdapter(adapter)
        etPatientName.setOnItemClickListener { _, _, position, _ ->
            selectedPatientId = patients[position].patientId
        }
    }

    private fun validateForm(): Boolean {
        if (selectedPatientId == null) {
            etPatientName.error = "Select patient"
            return false
        }
        if (etVisitDate.text.isNullOrBlank()) {
            etVisitDate.error = "Select visit date"
            return false
        }
        if (selectedHealth == null) {
            Snackbar.make(btnSave, "Select General Health", Snackbar.LENGTH_SHORT).show()
            return false
        }
        if (selectedDrugs == null) {
            Snackbar.make(btnSave, "Select drug usage", Snackbar.LENGTH_SHORT).show()
            return false
        }
        if (etComments.text.isNullOrBlank()) {
            etComments.error = "Comments required"
            return false
        }
        return true
    }
}
