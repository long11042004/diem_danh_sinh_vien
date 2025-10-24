package com.example.diemdanhsinhvien.activity

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.adapter.ClassReportDetailAdapter
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.network.apiservice.APIClient
import com.example.diemdanhsinhvien.repository.ReportRepository
import com.example.diemdanhsinhvien.viewmodel.ClassReportDetailViewModel
import com.example.diemdanhsinhvien.viewmodel.ClassReportDetailViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class ClassReportDetailActivity : AppCompatActivity() {

    private var classId: Int = -1
    private var className: String? = null

    private val viewModel: ClassReportDetailViewModel by viewModels {
        ClassReportDetailViewModelFactory(
            ReportRepository(
                attendanceApi = APIClient.attendanceApi(applicationContext),
                courseApi = APIClient.courseApi(applicationContext),
                studentApi = APIClient.studentApi(applicationContext),
            ),
            classId
        )
    }

    companion object {
        const val EXTRA_CLASS_ID = "extra_class_id"
        const val EXTRA_CLASS_NAME = "extra_class_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_report_detail)

        classId = intent.getIntExtra(EXTRA_CLASS_ID, -1)
        className = intent.getStringExtra(EXTRA_CLASS_NAME)

        if (classId == -1) {
            Toast.makeText(this, "Dữ liệu lớp học không hợp lệ.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarReportDetail)
        toolbar.title = className ?: getString(R.string.report_detail_title)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setupUi()
    }

    private fun setupUi() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewReportDetails)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarReportDetail)
        val noDetailsTextView = findViewById<TextView>(R.id.textViewNoDetails)
        val adapter = ClassReportDetailAdapter()

        recyclerView.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reportDetails.collect { state ->
                    progressBar.isVisible = state is UiState.Loading
                    recyclerView.isVisible = state is UiState.Success && state.data.isNotEmpty()
                    noDetailsTextView.isVisible = state is UiState.Success && state.data.isEmpty()

                    when (state) {
                        is UiState.Success -> adapter.submitList(state.data)
                        is UiState.Error -> {
                            noDetailsTextView.isVisible = true
                            noDetailsTextView.text = state.message
                        }
                        else -> { /* Do nothing for Loading or Empty */ }
                    }
                }
            }
        }
    }
}
