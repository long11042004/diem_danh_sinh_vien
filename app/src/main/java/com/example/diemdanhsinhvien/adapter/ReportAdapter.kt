package com.example.diemdanhsinhvien.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.diemdanhsinhvien.activity.ClassReportDetailActivity
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.data.model.Report

class ReportAdapter(private val onItemClicked: (Report) -> Unit) :
    ListAdapter<Report, ReportAdapter.ReportViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.report_item, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = getItem(position)
        holder.bind(report, onItemClicked)
    }

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewCourseName: TextView = itemView.findViewById(R.id.textViewCourseName)
        private val textViewClassCode: TextView = itemView.findViewById(R.id.textViewClassCode)
        private val textViewAttendanceRate: TextView = itemView.findViewById(R.id.textViewAttendanceRate)
        private val buttonDetails: Button = itemView.findViewById(R.id.buttonDetails)

        fun bind(report: Report, onItemClicked: (Report) -> Unit) {
            textViewCourseName.text = report.courseName
            textViewClassCode.text = "Mã lớp: ${report.classCode}"
        
            val formattedRate = String.format("%.1f%%", report.attendanceRate)
            textViewAttendanceRate.text = itemView.context.getString(R.string.attendance_rate_format, formattedRate)

            buttonDetails.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ClassReportDetailActivity::class.java).apply {
                    putExtra(ClassReportDetailActivity.EXTRA_CLASS_ID, report.classId)
                    putExtra(ClassReportDetailActivity.EXTRA_CLASS_NAME, report.courseName)
                }
                context.startActivity(intent)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Report>() {
            override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean {
                return oldItem.classId == newItem.classId
            }

            override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean {
                return oldItem == newItem
            }
        }
    }
}