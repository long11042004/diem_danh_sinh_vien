package com.example.diemdanhsinhvien.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
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
        const val EXTRA_STUDENT_NAME = "extra_student_name"
        const val EXTRA_STUDENT_CODE = "extra_student_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_detail)

        // Setup toolbar
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

        val studentName = intent.getStringExtra(EXTRA_STUDENT_NAME)
        val studentCode = intent.getStringExtra(EXTRA_STUDENT_CODE)

        val nameTextView = findViewById<TextView>(R.id.textViewStudentName)
        val idTextView = findViewById<TextView>(R.id.textViewStudentId)
        val avatarImageView = findViewById<ImageView>(R.id.imageViewAvatarDetail)

        nameTextView.text = studentName
        idTextView.text = getString(R.string.student_id_label_prefix, studentCode)
        avatarImageView.setImageResource(R.drawable.ic_student_avatar)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val historyAdapter = AttendanceHistoryAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAttendanceHistory)
        val noHistoryTextView = findViewById<TextView>(R.id.textViewNoHistory)

        recyclerView.adapter = historyAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.attendanceHistory.collect { historyList ->
                    historyAdapter.submitList(historyList)
                    val hasHistory = historyList.isNotEmpty()
                    recyclerView.isVisible = hasHistory
                    noHistoryTextView.isVisible = !hasHistory
                }
            }
        }
    }
}