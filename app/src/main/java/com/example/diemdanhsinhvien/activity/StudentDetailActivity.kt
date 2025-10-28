package com.example.diemdanhsinhvien.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.adapter.AttendanceHistoryAdapter
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.common.HistoryUiState
import com.example.diemdanhsinhvien.network.apiservice.APIClient
import com.example.diemdanhsinhvien.repository.StudentRepository
import com.example.diemdanhsinhvien.viewmodel.StudentDetailViewModel
import com.example.diemdanhsinhvien.viewmodel.StudentDetailViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class StudentDetailActivity : AppCompatActivity() {

    private var studentDbId: Int = -1

    private val viewModel: StudentDetailViewModel by viewModels {
        StudentDetailViewModelFactory(
            StudentRepository(
                studentApi = APIClient.studentApi(applicationContext),
                attendanceApi = APIClient.attendanceApi(applicationContext)
            ),
            studentDbId
        )
    }
    companion object {
        const val EXTRA_STUDENT_DB_ID = "extra_student_db_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_detail)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarStudentDetail)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        studentDbId = intent.getIntExtra(EXTRA_STUDENT_DB_ID, -1)
        if (studentDbId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy sinh viên.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val avatarImageView = findViewById<ImageView>(R.id.imageViewAvatarDetail)
        avatarImageView.setImageResource(R.drawable.ic_student_avatar)

        observeUi()
    }
    
    private fun observeUi() {
        val nameTextView = findViewById<TextView>(R.id.textViewStudentName)
        val idTextView = findViewById<TextView>(R.id.textViewStudentId)

        val emailTextView = findViewById<TextView>(R.id.textViewStudentEmail)
        val departmentTextView = findViewById<TextView>(R.id.textViewStudentDepartment)
        val classTextView = findViewById<TextView>(R.id.textViewStudentClass)

        val historyAdapter = AttendanceHistoryAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAttendanceHistory)
        val noHistoryTextView = findViewById<TextView>(R.id.textViewNoHistory)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarStudentDetail) // Dùng cho mục lịch sử
        val historyTitle = findViewById<TextView>(R.id.textViewHistoryTitle)
        val historyDivider = findViewById<View>(R.id.divider)

        recyclerView.adapter = historyAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.studentDetail.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                nameTextView.text = "Đang tải..."
                            }
                            is UiState.Success -> {
                                val student = state.data
                                nameTextView.text = student.studentName
                                idTextView.text = student.studentId
                                emailTextView.text = student.email
                                departmentTextView.text = student.department
                                classTextView.text = student.className
                            }
                            is UiState.Error -> {
                                nameTextView.text = "Lỗi tải thông tin"
                                Toast.makeText(this@StudentDetailActivity, state.message, Toast.LENGTH_LONG).show()
                            }
                            else -> { /* Không làm gì với các trạng thái khác */ }
                        }
                    }
                }

                launch {
                    viewModel.attendanceHistory.collect { state ->
                        // Mặc định hiển thị các thành phần của mục lịch sử
                        historyTitle.isVisible = true
                        historyDivider.isVisible = true

                        when (state) {
                            is HistoryUiState.Loading -> {
                                progressBar.isVisible = true
                                recyclerView.isVisible = false
                                noHistoryTextView.isVisible = false
                            }
                            is HistoryUiState.Success -> {
                                progressBar.isVisible = false
                                val historyList = state.data
                                val hasHistory = historyList.isNotEmpty()
                                recyclerView.isVisible = hasHistory
                                noHistoryTextView.isVisible = !hasHistory

                                noHistoryTextView.text = "Chưa có lịch sử điểm danh."
                                historyAdapter.submitList(historyList)
                            }
                            is HistoryUiState.Error -> {
                                progressBar.isVisible = false
                                recyclerView.isVisible = false
                                noHistoryTextView.isVisible = true
                                noHistoryTextView.text = state.message
                            }
                            is HistoryUiState.Hidden -> {
                                // Ẩn toàn bộ mục lịch sử
                                progressBar.isVisible = false
                                historyTitle.isVisible = false
                                historyDivider.isVisible = false
                                recyclerView.isVisible = false
                                noHistoryTextView.isVisible = false
                            }
                        }
                    }
                }
            }
        }
    }
}