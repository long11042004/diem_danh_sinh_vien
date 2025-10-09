package com.example.diemdanhsinhvien.fragment

import android.os.Bundle
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.adapter.ReportAdapter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.example.diemdanhsinhvien.model.Report
import com.example.diemdanhsinhvien.network.APIClient
import com.example.diemdanhsinhvien.repository.ReportRepository
import com.example.diemdanhsinhvien.viewmodel.ReportViewModel
import com.example.diemdanhsinhvien.viewmodel.ReportViewModelFactory

class ReportsFragment : Fragment() {

    private val reportViewModel: ReportViewModel by viewModels {
        ReportViewModelFactory(
            ReportRepository(
                courseApi = APIClient.courseApi,
                studentApi = APIClient.studentApi,
                attendanceApi = APIClient.attendanceApi
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("ReportsFragment", "onViewCreated called")
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewReports)
        val textViewNoReports = view.findViewById<TextView>(R.id.textViewNoReports)
        val barChart = view.findViewById<BarChart>(R.id.barChartAttendance)

        val adapter = ReportAdapter { report ->
            exportReport(report)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        reportViewModel.reports.observe(viewLifecycleOwner) { reports ->
            Log.d("ReportsFragment", "Reports observed: ${reports.size} reports")
            val hasReports = reports.isNotEmpty()
            recyclerView.isVisible = hasReports
            textViewNoReports.isVisible = !hasReports
            barChart.isVisible = hasReports
            adapter.submitList(reports)

            if (hasReports) {
                setupBarChart(barChart, reports)
            }
        }
    }

    private fun setupBarChart(barChart: BarChart, reports: List<Report>) {
         Log.d("ReportsFragment", "Setting up bar chart with ${reports.size} reports")
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        reports.forEachIndexed { index, report ->
            entries.add(BarEntry(index.toFloat(), report.attendanceRate.toFloat()))
            labels.add(report.courseName)
        }

        val barDataSet = BarDataSet(entries, "Tỷ lệ điểm danh (%)")
        barDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        barDataSet.valueTextColor = Color.BLACK
        barDataSet.valueTextSize = 10f

        val barData = BarData(barDataSet)
        barChart.data = barData

        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.animateY(1000)

        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            granularity = 1f
            setCenterAxisLabels(false)
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
        }
    }

    private fun exportReport(report: Report) {
        Log.i("ReportsFragment", "Exporting report for course: ${report.courseName}")
        // TODO: Triển khai logic xuất file báo cáo (ví dụ: tạo file Excel/PDF)
        Toast.makeText(context, "Đang xuất báo cáo cho lớp: ${report.courseName}", Toast.LENGTH_SHORT).show()
    }
}