package com.example.diemdanhsinhvien.fragment

import android.os.Bundle
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.view.ViewGroup
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.model.Account
import com.example.diemdanhsinhvien.data.model.Report
import com.example.diemdanhsinhvien.network.apiservice.APIClient
import com.example.diemdanhsinhvien.repository.AccountRepository
import com.example.diemdanhsinhvien.repository.ReportRepository
import com.example.diemdanhsinhvien.viewmodel.AuthViewModel
import com.example.diemdanhsinhvien.viewmodel.AuthViewModelFactory
import com.example.diemdanhsinhvien.viewmodel.ReportViewModel
import com.example.diemdanhsinhvien.viewmodel.ReportViewModelFactory

class ReportsFragment : Fragment() {

    private val reportViewModel: ReportViewModel by viewModels {
        ReportViewModelFactory(
            ReportRepository(
                courseApi = APIClient.courseApi(requireContext()),
                studentApi = APIClient.studentApi(requireContext()),
                attendanceApi = APIClient.attendanceApi(requireContext())
            )
        )
    }

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            AccountRepository(
                accountApi = APIClient.accountApi(requireContext())
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
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarReports)
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayoutReports)

        val adapter = ReportAdapter { report ->
            exportReport(report)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        swipeRefreshLayout.setOnRefreshListener {
            Log.d("ReportsFragment", "Swipe to refresh triggered.")
            authViewModel.getAccountDetails()
        }

        setupObservers(recyclerView, textViewNoReports, barChart, progressBar, adapter, swipeRefreshLayout)

        authViewModel.getAccountDetails()
    }

    private fun setupObservers(
        recyclerView: RecyclerView,
        textViewNoReports: TextView,
        barChart: BarChart,
        progressBar: ProgressBar,
        adapter: ReportAdapter,
        swipeRefreshLayout: SwipeRefreshLayout
    ) {
        reportViewModel.reports.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    Log.d("ReportsFragment", "Đang tải báo cáo...")
                    if (adapter.currentList.isEmpty()) {
                        progressBar.isVisible = true
                        recyclerView.isVisible = false
                        textViewNoReports.isVisible = false
                        barChart.isVisible = true
                    }
                }
                is UiState.Success -> {
                    val reports = state.data
                    progressBar.isVisible = false
                    swipeRefreshLayout.isRefreshing = false
                    Log.d("ReportsFragment", "Quan sát thấy báo cáo: ${reports.size} báo cáo")
                    val hasReports = reports.isNotEmpty()
                    recyclerView.isVisible = hasReports
                    textViewNoReports.isVisible = !hasReports
                    barChart.isVisible = hasReports
                    adapter.submitList(reports)
                    if (hasReports) {
                        setupBarChart(barChart, reports)
                    }
                }
                is UiState.Error -> {
                    Log.e("ReportsFragment", "Lỗi: ${state.message}")
                    progressBar.isVisible = false
                    swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    recyclerView.isVisible = false
                    barChart.isVisible = true
                    textViewNoReports.isVisible = true
                }
                else -> {
                    progressBar.isVisible = false
                    swipeRefreshLayout.isRefreshing = false
                    Log.w("ReportsFragment", "Quan sát thấy trạng thái null hoặc không được xử lý.")
                    recyclerView.isVisible = false
                    textViewNoReports.isVisible = true
                }
            }
        }

        authViewModel.accountDetails.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    state.data?.let { account ->
                        Log.d("ReportsFragment", "Account loaded, fetching reports for teacher ID: ${account.id}")
                        reportViewModel.fetchReports(account.id)
                    }
                }
                is UiState.Error -> {
                    Log.e("ReportsFragment", "Failed to load account details: ${state.message}")
                    Toast.makeText(context, "Không thể tải thông tin tài khoản.", Toast.LENGTH_SHORT).show()
                }
                else -> { /* Đang tải hoặc trạng thái khác */ }
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
        barDataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String =
                "${String.format("%.1f", value)}%"
        }
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
        Toast.makeText(context, "Đang xuất báo cáo cho lớp: ${report.courseName}", Toast.LENGTH_SHORT).show()
    }
}