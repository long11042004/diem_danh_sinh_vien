package com.example.diemdanhsinhvien.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.database.relations.StudentAttendanceHistory
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceHistoryAdapter : ListAdapter<StudentAttendanceHistory, AttendanceHistoryAdapter.HistoryViewHolder>(HistoryDiffCallback) {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.textViewSessionDate)
        private val statusChip: Chip = itemView.findViewById(R.id.chipStatus)
        private val context: Context = itemView.context

        fun bind(history: StudentAttendanceHistory) {
            val sdf = SimpleDateFormat("dd 'tháng' MM, yyyy", Locale.getDefault())
            dateTextView.text = sdf.format(Date(history.date))
            when (history.status) {
                AttendanceStatus.PRESENT -> {
                    statusChip.text = context.getString(R.string.status_present)
                    statusChip.setChipBackgroundColorResource(R.color.light_green)
                }
                AttendanceStatus.ABSENT -> {
                    statusChip.text = context.getString(R.string.status_absent)
                    statusChip.setChipBackgroundColorResource(R.color.light_red)
                }
                AttendanceStatus.LATE -> {
                    statusChip.text = context.getString(R.string.status_late)
                    statusChip.setChipBackgroundColorResource(R.color.light_yellow)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.attendance_history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object HistoryDiffCallback : DiffUtil.ItemCallback<StudentAttendanceHistory>() {
        override fun areItemsTheSame(oldItem: StudentAttendanceHistory, newItem: StudentAttendanceHistory): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: StudentAttendanceHistory, newItem: StudentAttendanceHistory): Boolean {
            return oldItem == newItem
        }
    }
}