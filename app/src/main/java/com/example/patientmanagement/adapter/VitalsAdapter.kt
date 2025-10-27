package com.example.patientmanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.patientmanagement.R
import com.example.patientmanagement.model.Vitals
import java.text.SimpleDateFormat
import java.util.*

class VitalsAdapter : RecyclerView.Adapter<VitalsAdapter.VitalsViewHolder>() {

    private var vitalsList: List<Vitals> = emptyList()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun submitList(newList: List<Vitals>) {
        vitalsList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VitalsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vital, parent, false)
        return VitalsViewHolder(view)
    }

    override fun onBindViewHolder(holder: VitalsViewHolder, position: Int) {
        val vitals = vitalsList[position]
        
        holder.tvVitalDate.text = dateFormat.format(vitals.visitDate)
        holder.tvHeight.text = "${vitals.heightCm} cm"
        holder.tvWeight.text = "${vitals.weightKg} kg"
        holder.tvBmi.text = String.format("%.1f", vitals.bmi)
        
        val bmiStatus = getBmiStatus(vitals.bmi)
        holder.tvBmiStatus.text = bmiStatus
        
        // Set text color based on BMI status
        val colorRes = when (bmiStatus) {
            "Normal" -> R.color.green
            else -> R.color.red
        }
        
        holder.tvBmiStatus.setTextColor(
            ContextCompat.getColor(holder.itemView.context, colorRes)
        )
    }

    override fun getItemCount(): Int = vitalsList.size

    private fun getBmiStatus(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Normal"
            else -> "Overweight"
        }
    }

    inner class VitalsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvVitalDate: TextView = view.findViewById(R.id.tvVitalDate)
        val tvHeight: TextView = view.findViewById(R.id.tvHeight)
        val tvWeight: TextView = view.findViewById(R.id.tvWeight)
        val tvBmi: TextView = view.findViewById(R.id.tvBmi)
        val tvBmiStatus: TextView = view.findViewById(R.id.tvBmiStatus)
    }
}

