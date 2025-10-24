package com.example.patientmanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.patientmanagement.adapter.PatientAdapter
import com.example.patientmanagement.adapter.PatientWithVitals
import com.example.patientmanagement.data.AppDatabase
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerPatients: RecyclerView
    private lateinit var btnRegister: MaterialButton
    private lateinit var etFilterDate: TextInputEditText
    private lateinit var btnClearFilter: MaterialButton
    private lateinit var patientAdapter: PatientAdapter

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var selectedDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerPatients = findViewById(R.id.recyclerClients)
        btnRegister = findViewById(R.id.btnRegisterNewClient)
        etFilterDate = findViewById(R.id.etDateFilter)
        btnClearFilter = findViewById(R.id.btnClearFilter)

        // Initialize RecyclerView & Adapter
        patientAdapter = PatientAdapter()
        recyclerPatients.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = patientAdapter
        }

        // Observe all patients initially
        observePatients()

        // Navigate to RegisterPatient form
        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterPatient::class.java))
        }

        // Open MaterialDatePicker when user taps the filter field
        etFilterDate.setOnClickListener {
            showMaterialDatePicker()
        }

        // Clear filter and reload all patients
        btnClearFilter.setOnClickListener {
            selectedDate = null
            etFilterDate.setText("")
            observePatients()
        }
    }

    override fun onResume() {
        super.onResume()
        if (selectedDate == null) observePatients()
    }

    // Load all patients with their latest vitals
    private fun observePatients() {
        val db = AppDatabase.getDatabase(applicationContext)
        lifecycleScope.launch(Dispatchers.IO) {
            db.patientDao().getAllPatients().collectLatest { patients ->
                val patientWithVitalsList = patients.map { patient ->
                    val lastVitals = db.vitalsDao().getLastVitalsByPatientId(patient.patientId)
                    PatientWithVitals(
                        patient = patient,
                        lastBmi = lastVitals?.bmi
                    )
                }

                withContext(Dispatchers.Main) {
                    Log.d("MainActivity", "Loaded ${patientWithVitalsList.size} patients")
                    patientAdapter.submitList(patientWithVitalsList)
                }
            }
        }
    }

    // Filter patients based on selected visit date
    private fun filterPatientsByDate(date: Date) {
        val db = AppDatabase.getDatabase(applicationContext)

        lifecycleScope.launch(Dispatchers.IO) {
            val vitalsOnDate = db.vitalsDao().getVitalsByDate(date.time)
            val patientIds = vitalsOnDate.map { it.patientId }.distinct()

            db.patientDao().getAllPatients().collectLatest { patients ->
                val filteredPatients = patients.filter { it.patientId in patientIds }

                val patientWithVitalsList = filteredPatients.map { patient ->
                    val lastVitals = db.vitalsDao().getLastVitalsByPatientId(patient.patientId)
                    PatientWithVitals(
                        patient = patient,
                        lastBmi = lastVitals?.bmi
                    )
                }

                withContext(Dispatchers.Main) {
                    patientAdapter.submitList(patientWithVitalsList)
                }
            }
        }
    }

    // Use MaterialDatePicker for a modern look
    private fun showMaterialDatePicker() {
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Visit Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

        datePicker.show(supportFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            selectedDate = calendar.time
            etFilterDate.setText(dateFormat.format(selectedDate!!))
            filterPatientsByDate(selectedDate!!)
        }
    }
}
