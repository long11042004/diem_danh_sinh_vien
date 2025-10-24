package com.example.diemdanhsinhvien.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.data.model.ClassReportDetail
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClassReportDetailAdapter : ListAdapter<ClassReportDetail, ClassReportDetailAdapter.ReportDetailViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportDetailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.class_report_detail_item, parent, false)
        return ReportDetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportDetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReportDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sessionDateTextView: TextView = itemView.findViewById(R.id.textViewSessionDate)
        private val presentCountTextView: TextView = itemView.findViewById(R.id.textViewPresentCount)
        private val absentCountTextView: TextView = itemView.findViewById(R.id.textViewAbsentCount)
        private val lateCountTextView: TextView = itemView.findViewById(R.id.textViewLateCount)
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(reportDetail: ClassReportDetail) {
            val context = itemView.context

            val date = Date(reportDetail.sessionDate)
            sessionDateTextView.text = context.getString(R.string.session_date_format, dateFormat.format(date))

            presentCountTextView.text = context.getString(R.string.present_count_format, reportDetail.presentCount.toIntOrNull() ?: 0)
            absentCountTextView.text = context.getString(R.string.absent_count_format, reportDetail.absentCount.toIntOrNull() ?: 0)
            lateCountTextView.text = context.getString(R.string.late_count_format, reportDetail.lateCount.toIntOrNull() ?: 0)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ClassReportDetail>() {
        override fun areItemsTheSame(oldItem: ClassReportDetail, newItem: ClassReportDetail): Boolean {
            return oldItem.sessionDate == newItem.sessionDate
        }

        override fun areContentsTheSame(oldItem: ClassReportDetail, newItem: ClassReportDetail): Boolean {
            return oldItem == newItem
        }
    }
}
