package com.example.diemdanhsinhvien.activity

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.adapter.ClassReportDetailAdapter
import com.example.diemdanhsinhvien.data.model.ClassReportDetail
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.network.apiservice.APIClient
import com.example.diemdanhsinhvien.repository.ClassRepository
import com.example.diemdanhsinhvien.repository.ReportRepository
import com.example.diemdanhsinhvien.viewmodel.ClassReportDetailViewModel
import com.example.diemdanhsinhvien.viewmodel.ClassReportDetailViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class ClassReportDetailActivity : AppCompatActivity() {

    private var classId: Int = -1
    private var className: String? = null

    private lateinit var courseNameTextView: TextView
    private lateinit var classCodeTextView: TextView
    private lateinit var courseIdTextView: TextView
    private lateinit var semesterTextView: TextView
    private lateinit var studentCountTextView: TextView

    private val createFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let {
            val currentState = viewModel.reportDetails.value
            if (currentState is UiState.Success) {
                val reportData = currentState.data
                val csvContent = generateCsvContent(reportData)
                writeCsvToFile(it, csvContent)
            }
        }
    }

    private val viewModel: ClassReportDetailViewModel by viewModels {
        ClassReportDetailViewModelFactory(
            ReportRepository(
                attendanceApi = APIClient.attendanceApi(applicationContext),
                courseApi = APIClient.courseApi(applicationContext),
                studentApi = APIClient.studentApi(applicationContext),
            ),
            ClassRepository(
                courseApi = APIClient.courseApi(applicationContext)
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
        val classDetailsCard = findViewById<View>(R.id.classDetailsCard)

        // Khởi tạo các TextView trong card thông tin lớp học
        courseNameTextView = classDetailsCard.findViewById(R.id.textViewCourseName)
        classCodeTextView = classDetailsCard.findViewById(R.id.textViewClassCode)
        courseIdTextView = classDetailsCard.findViewById(R.id.textViewCourseId)
        semesterTextView = classDetailsCard.findViewById(R.id.textViewSemester)
        studentCountTextView = classDetailsCard.findViewById(R.id.textViewStudentCount)

        val buttonStartAttendance = classDetailsCard.findViewById<MaterialButton>(R.id.buttonStartAttendance)
        val buttonExportReport = classDetailsCard.findViewById<MaterialButton>(R.id.buttonExportReport)

        val qrAttendanceButton = classDetailsCard.findViewById<MaterialButton>(R.id.buttonStartQRAttendance)
        buttonStartAttendance.visibility = View.GONE
        buttonExportReport.visibility = View.VISIBLE
        qrAttendanceButton.visibility = View.GONE // Ẩn nút Điểm danh bằng QR
        
        buttonExportReport.setOnClickListener {
            val currentState = viewModel.reportDetails.value
            if (currentState is UiState.Success && currentState.data.isNotEmpty()) {
                // Tạo tên tệp từ tên lớp học, thay thế các ký tự không hợp lệ
                val safeClassName = className?.replace(Regex("[^a-zA-Z0-9_]"), "_") ?: "lop_hoc"
                createFileLauncher.launch("Bao_cao_diem_danh_$safeClassName.csv")
            } else {
                Toast.makeText(this, "Không có dữ liệu để xuất báo cáo.", Toast.LENGTH_SHORT).show()
            }
        }

        recyclerView.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
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

                launch {
                    viewModel.classDetails.collect { classDetails ->
                        classDetails?.let {
                            courseNameTextView.text = it.courseName
                            classCodeTextView.text = getString(R.string.class_code_label, it.classCode)
                            courseIdTextView.text = getString(R.string.course_id_label, it.courseId)
                            semesterTextView.text = getString(R.string.semester_label, it.semester)
                        }
                    }
                }
            }
        }
    }

    private fun generateCsvContent(data: List<ClassReportDetail>): String {
        val stringBuilder = StringBuilder()

        // Thêm thông tin chi tiết của lớp học vào đầu tệp CSV
        // Đặt giá trị trong dấu ngoặc kép để xử lý trường hợp chứa dấu phẩy
        stringBuilder.append("Tên môn học:,\"${courseNameTextView.text}\"\n")
        stringBuilder.append("\"${classCodeTextView.text}\"\n")
        stringBuilder.append("\"${courseIdTextView.text}\"\n")
        stringBuilder.append("\"${semesterTextView.text}\"\n\n")

        // Thêm phần đầu của bảng dữ liệu
        val header = "Ngày,Có mặt,Vắng,Trễ\n"
        stringBuilder.append(header)

        // Thêm các hàng dữ liệu điểm danh theo từng buổi
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        data.forEach { detail ->
            val dateString = try {
                isoFormat.parse(detail.sessionDate)?.let { date ->
                    displayFormat.format(date)
                } ?: detail.sessionDate // Dùng giá trị gốc nếu parse trả về null
            } catch (e: Exception) {
                e.printStackTrace()
                detail.sessionDate // Dùng giá trị gốc nếu có lỗi parse
            }
            stringBuilder.append("$dateString,${detail.presentCount},${detail.absentCount},${detail.lateCount}\n")
        }
        return stringBuilder.toString()
    }

    private fun writeCsvToFile(uri: Uri, content: String) {
        try {
            // Sử dụng UTF-8 with BOM để Excel mở file tiếng Việt có dấu đúng cách
            val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(bom)
                outputStream.write(content.toByteArray(Charsets.UTF_8))
            }
            Toast.makeText(this, "Xuất báo cáo thành công!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi khi lưu tệp báo cáo.", Toast.LENGTH_SHORT).show()
        }
    }
}
