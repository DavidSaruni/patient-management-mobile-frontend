package com.example.patientmanagement.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.patientmanagement.R
import com.example.patientmanagement.PatientDetailsActivity
import com.example.patientmanagement.model.Patient
import java.util.*

data class PatientWithVitals(
    val patient: Patient,
    val lastBmi: Double?
)

class PatientAdapter : RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    private var patientsWithVitals: List<PatientWithVitals> = emptyList()

    fun submitList(newList: List<PatientWithVitals>) {
        patientsWithVitals = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patientWithVitals = patientsWithVitals[position]
        val patient = patientWithVitals.patient
        val fullName = "${patient.firstName} ${patient.lastName}"
        val age = calculateAge(patient.dateOfBirth)
        val bmi = patientWithVitals.lastBmi ?: 0.0
        val bmiStatus = getBmiStatus(bmi)

        holder.tvIndex.text = (position + 1).toString()
        holder.tvName.text = fullName
        holder.tvAge.text = age.toString()
        holder.tvBmiStatus.text = String.format("%.1f (%s)", bmi, bmiStatus)

        // ✅ Set text color based on BMI status
        val colorRes = when (bmiStatus) {
            "Normal" -> R.color.green
            else -> R.color.red
        }

        holder.tvBmiStatus.setTextColor(
            ContextCompat.getColor(holder.itemView.context, colorRes)
        )

        // Set click listener for the entire item
        holder.itemView.setOnClickListener {
            Log.d("PatientAdapter", "Patient clicked: ${patient.patientId}")
            val intent = Intent(holder.itemView.context, PatientDetailsActivity::class.java)
            intent.putExtra("patientId", patient.patientId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = patientsWithVitals.size

    private fun calculateAge(dateOfBirth: Date): Int {
        val now = Calendar.getInstance()
        val dob = Calendar.getInstance().apply { time = dateOfBirth }
        var age = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (now.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) age--
        return age
    }

    private fun getBmiStatus(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Normal"
            else -> "Overweight"
        }
    }

    inner class PatientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIndex: TextView = view.findViewById(R.id.tvIndex)
        val tvName: TextView = view.findViewById(R.id.tvPatientName)
        val tvAge: TextView = view.findViewById(R.id.tvAge)
        val tvBmiStatus: TextView = view.findViewById(R.id.tvBmiStatus)
    }
}
