package com.example.diemdanhsinhvien.activity

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.adapter.AttendanceAdapter
import com.example.diemdanhsinhvien.repository.ClassRepository
import com.example.diemdanhsinhvien.repository.AttendanceRepository
import com.example.diemdanhsinhvien.repository.StudentRepository
import com.example.diemdanhsinhvien.viewmodel.AttendanceViewModel
import com.example.diemdanhsinhvien.viewmodel.AttendanceViewModelFactory
import com.example.diemdanhsinhvien.viewmodel.StudentViewModel
import com.example.diemdanhsinhvien.viewmodel.StudentViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class AttendanceActivity : AppCompatActivity() {

    private var classId: Int = -1
    private lateinit var attendanceAdapter: AttendanceAdapter

    private val studentViewModel: StudentViewModel by viewModels {
        StudentViewModelFactory(
            studentRepository = StudentRepository(),
            classRepository = ClassRepository(),
            classId = classId
        )
    }

    private val attendanceViewModel: AttendanceViewModel by viewModels {
        AttendanceViewModelFactory(
            AttendanceRepository()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        classId = intent.getIntExtra(EXTRA_CLASS_ID, -1)
        if (classId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID lớp học.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()

        findViewById<Button>(R.id.buttonSaveAttendance).setOnClickListener {
            val results = attendanceAdapter.getAttendanceResults()
            attendanceViewModel.saveAttendance(classId, results)

            Toast.makeText(this, getString(R.string.attendance_saved_successfully), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarAttendance)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        attendanceAdapter = AttendanceAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAttendance)
        recyclerView.adapter = attendanceAdapter
        recyclerView.layoutManager = LinearLayoutManager(this@AttendanceActivity)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                studentViewModel.students.collect { students ->
                    attendanceAdapter.submitList(students)
                }
            }
        }
    }

    companion object {
        const val EXTRA_CLASS_ID = "extra_class_id_attendance"
    }
}