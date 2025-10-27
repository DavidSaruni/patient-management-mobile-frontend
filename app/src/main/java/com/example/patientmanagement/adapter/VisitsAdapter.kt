package com.example.patientmanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.patientmanagement.R
import com.example.patientmanagement.model.VisitA
import com.example.patientmanagement.model.VisitB
import java.text.SimpleDateFormat
import java.util.*

sealed class VisitItem {
    data class VisitAItem(val visit: VisitA) : VisitItem()
    data class VisitBItem(val visit: VisitB) : VisitItem()
}

class VisitsAdapter : RecyclerView.Adapter<VisitsAdapter.VisitsViewHolder>() {

    private var visitsList: List<VisitItem> = emptyList()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun submitList(newList: List<VisitItem>) {
        visitsList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_visit, parent, false)
        return VisitsViewHolder(view)
    }

    override fun onBindViewHolder(holder: VisitsViewHolder, position: Int) {
        val visitItem = visitsList[position]
        
        when (visitItem) {
            is VisitItem.VisitAItem -> {
                val visit = visitItem.visit
                holder.tvVisitDate.text = dateFormat.format(visit.visitDate)
                holder.tvVisitType.text = "Visit A"
                holder.tvGeneralHealth.text = visit.generalHealth
                holder.tvAdditionalInfoLabel.text = "On Diet:"
                holder.tvAdditionalInfo.text = visit.onDiet
                holder.tvComments.text = visit.comments
            }
            is VisitItem.VisitBItem -> {
                val visit = visitItem.visit
                holder.tvVisitDate.text = dateFormat.format(visit.visitDate)
                holder.tvVisitType.text = "Visit B"
                holder.tvGeneralHealth.text = visit.generalHealth
                holder.tvAdditionalInfoLabel.text = "Using Drugs:"
                holder.tvAdditionalInfo.text = visit.usingDrugs
                holder.tvComments.text = visit.comments
            }
        }
    }

    override fun getItemCount(): Int = visitsList.size

    inner class VisitsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvVisitDate: TextView = view.findViewById(R.id.tvVisitDate)
        val tvVisitType: TextView = view.findViewById(R.id.tvVisitType)
        val tvGeneralHealth: TextView = view.findViewById(R.id.tvGeneralHealth)
        val tvAdditionalInfoLabel: TextView = view.findViewById(R.id.tvAdditionalInfoLabel)
        val tvAdditionalInfo: TextView = view.findViewById(R.id.tvAdditionalInfo)
        val tvComments: TextView = view.findViewById(R.id.tvComments)
    }
}

