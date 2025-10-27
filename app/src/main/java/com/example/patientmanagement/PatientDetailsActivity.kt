package com.example.patientmanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.example.patientmanagement.adapter.VitalsAdapter
import com.example.patientmanagement.adapter.VisitsAdapter
import com.example.patientmanagement.adapter.VisitItem
import com.example.patientmanagement.data.AppDatabase
import com.example.patientmanagement.model.Patient
import com.example.patientmanagement.model.Vitals
import com.example.patientmanagement.model.VisitA
import com.example.patientmanagement.model.VisitB
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PatientDetailsActivity : AppCompatActivity() {

    private lateinit var tvPatientName: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvRegistrationDate: TextView
    private lateinit var recyclerVitals: RecyclerView
    private lateinit var recyclerVisits: RecyclerView
    private lateinit var tvNoVitals: TextView
    private lateinit var tvNoVisits: TextView
    private lateinit var btnAddVitals: MaterialButton
    private lateinit var btnAddVisit: MaterialButton

    private lateinit var vitalsAdapter: VitalsAdapter
    private lateinit var visitsAdapter: VisitsAdapter
    private lateinit var database: AppDatabase

    private var patient: Patient? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("PatientDetailsActivity", "onCreate called")
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        initializeDatabase()
        initializeAdapters()
        setupClickListeners()
        loadPatientData()
        Log.d("PatientDetailsActivity", "onCreate completed")
    }

    private fun initializeViews() {
        tvPatientName = findViewById(R.id.tvPatientName)
        tvFullName = findViewById(R.id.tvFullName)
        tvAge = findViewById(R.id.tvAge)
        tvGender = findViewById(R.id.tvGender)
        tvRegistrationDate = findViewById(R.id.tvRegistrationDate)
        recyclerVitals = findViewById(R.id.recyclerVitals)
        recyclerVisits = findViewById(R.id.recyclerVisits)
        tvNoVitals = findViewById(R.id.tvNoVitals)
        tvNoVisits = findViewById(R.id.tvNoVisits)
        btnAddVitals = findViewById(R.id.btnAddVitals)
        btnAddVisit = findViewById(R.id.btnAddVisit)

        // Back button
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun initializeDatabase() {
        database = AppDatabase.getDatabase(this)
    }

    private fun initializeAdapters() {
        vitalsAdapter = VitalsAdapter()
        visitsAdapter = VisitsAdapter()

        recyclerVitals.apply {
            layoutManager = LinearLayoutManager(this@PatientDetailsActivity)
            adapter = vitalsAdapter
        }

        recyclerVisits.apply {
            layoutManager = LinearLayoutManager(this@PatientDetailsActivity)
            adapter = visitsAdapter
        }
    }

    private fun setupClickListeners() {
        btnAddVitals.setOnClickListener {
            patient?.let { p ->
                val intent = Intent(this, VitalsActivity::class.java)
                intent.putExtra("patientId", p.patientId)
                startActivity(intent)
            }
        }

        btnAddVisit.setOnClickListener {
            patient?.let { p ->
                // Determine which visit form to show based on latest BMI
                lifecycleScope.launch {
                    try {
                        val latestVitals = database.vitalsDao().getLastVitalsByPatientId(p.patientId)
                        val bmi = latestVitals?.bmi ?: 0.0
                        
                        Log.d("PatientDetailsActivity", "Patient BMI: $bmi, showing ${if (bmi >= 25.0) "Visit B" else "Visit A"}")
                        
                        val intent = if (bmi >= 25.0) {
                            Intent(this@PatientDetailsActivity, VisitPageBActivity::class.java)
                        } else {
                            Intent(this@PatientDetailsActivity, VisitPageAActivity::class.java)
                        }
                        
                        intent.putExtra("patientId", p.patientId)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("PatientDetailsActivity", "Error determining visit form", e)
                        // Default to Visit A if there's an error
                        val intent = Intent(this@PatientDetailsActivity, VisitPageAActivity::class.java)
                        intent.putExtra("patientId", p.patientId)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun loadPatientData() {
        val patientId = intent.getStringExtra("patientId")
        Log.d("PatientDetailsActivity", "Loading patient data for ID: $patientId")
        if (patientId != null) {
            lifecycleScope.launch {
                try {
                    patient = database.patientDao().getPatientById(patientId)
                    Log.d("PatientDetailsActivity", "Patient found: ${patient?.firstName}")
                    patient?.let { p ->
                        displayPatientInfo(p)
                        loadVitals(p.patientId)
                        loadVisits(p.patientId)
                        updateVisitButtonText(p.patientId)
                    }
                } catch (e: Exception) {
                    Log.e("PatientDetailsActivity", "Error loading patient data", e)
                    e.printStackTrace()
                    // Handle error - maybe show a message or finish activity
                    finish()
                }
            }
        } else {
            Log.e("PatientDetailsActivity", "No patient ID provided")
            // No patient ID provided, finish activity
            finish()
        }
    }

    private fun displayPatientInfo(patient: Patient) {
        val fullName = "${patient.firstName} ${patient.lastName}"
        val age = calculateAge(patient.dateOfBirth)
        
        tvPatientName.text = fullName
        tvFullName.text = fullName
        tvAge.text = "$age years"
        tvGender.text = patient.gender
        tvRegistrationDate.text = dateFormat.format(patient.registrationDate)
    }

    private fun loadVitals(patientId: String) {
        lifecycleScope.launch {
            try {
                val vitals = database.vitalsDao().getVitalsByPatientId(patientId)
                if (vitals.isNotEmpty()) {
                    vitalsAdapter.submitList(vitals.sortedByDescending { it.visitDate })
                    tvNoVitals.visibility = View.GONE
                    recyclerVitals.visibility = View.VISIBLE
                } else {
                    tvNoVitals.visibility = View.VISIBLE
                    recyclerVitals.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                tvNoVitals.visibility = View.VISIBLE
                recyclerVitals.visibility = View.GONE
            }
        }
    }

    private fun loadVisits(patientId: String) {
        lifecycleScope.launch {
            try {
                val visitsA = database.visitADao().getVisitsByPatientId(patientId)
                val visitsB = database.visitBDao().getVisitsByPatientId(patientId)
                
                val allVisits = mutableListOf<VisitItem>()
                allVisits.addAll(visitsA.map { VisitItem.VisitAItem(it) })
                allVisits.addAll(visitsB.map { VisitItem.VisitBItem(it) })
                
                // Sort by date (most recent first)
                allVisits.sortByDescending { visitItem ->
                    when (visitItem) {
                        is VisitItem.VisitAItem -> visitItem.visit.visitDate
                        is VisitItem.VisitBItem -> visitItem.visit.visitDate
                    }
                }
                
                if (allVisits.isNotEmpty()) {
                    visitsAdapter.submitList(allVisits)
                    tvNoVisits.visibility = View.GONE
                    recyclerVisits.visibility = View.VISIBLE
                } else {
                    tvNoVisits.visibility = View.VISIBLE
                    recyclerVisits.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                tvNoVisits.visibility = View.VISIBLE
                recyclerVisits.visibility = View.GONE
            }
        }
    }

    private fun calculateAge(dateOfBirth: Date): Int {
        val now = Calendar.getInstance()
        val dob = Calendar.getInstance().apply { time = dateOfBirth }
        var age = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (now.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) age--
        return age
    }

    private fun updateVisitButtonText(patientId: String) {
        lifecycleScope.launch {
            try {
                val latestVitals = database.vitalsDao().getLastVitalsByPatientId(patientId)
                val bmi = latestVitals?.bmi ?: 0.0
                
                val buttonText = if (bmi >= 25.0) {
                    "Add Visit"
                } else {
                    "Add Visit"
                }
                
                btnAddVisit.text = buttonText
            } catch (e: Exception) {
                Log.e("PatientDetailsActivity", "Error updating visit button text", e)
                btnAddVisit.text = "Add Visit"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        patient?.let { p ->
            loadVitals(p.patientId)
            loadVisits(p.patientId)
            updateVisitButtonText(p.patientId)
        }
    }
}

