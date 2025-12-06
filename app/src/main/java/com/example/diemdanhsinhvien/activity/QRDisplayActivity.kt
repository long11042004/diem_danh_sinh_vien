package com.example.diemdanhsinhvien.activity

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.model.AttendanceSession
import com.example.diemdanhsinhvien.network.apiservice.APIClient
import com.example.diemdanhsinhvien.repository.AttendanceRepository
import com.example.diemdanhsinhvien.repository.ClassRepository
import com.example.diemdanhsinhvien.viewmodel.QRDisplayViewModel
import com.example.diemdanhsinhvien.viewmodel.QRDisplayViewModelFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

class QRDisplayActivity : AppCompatActivity() {

    private lateinit var imageViewQRCode: ImageView
    private lateinit var textViewCourseNameQR: TextView
    private lateinit var textViewSessionCode: TextView
    private lateinit var textViewSessionCodeInstruction: TextView
    private lateinit var textViewTimer: TextView
    private lateinit var textViewTimeUp: TextView
    private lateinit var loadingView: View
    private lateinit var errorView: View
    private lateinit var errorTextView: TextView
    private lateinit var retryButton: Button

    private var countDownTimer: CountDownTimer? = null
    private var classId: Int = -1

    private val viewModel: QRDisplayViewModel by viewModels {
        QRDisplayViewModelFactory(
            classId,
            AttendanceRepository(APIClient.attendanceApi(applicationContext)),
            ClassRepository(APIClient.courseApi(applicationContext))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_display)

        imageViewQRCode = findViewById(R.id.imageViewQRCode)
        textViewCourseNameQR = findViewById(R.id.textViewCourseNameQR)
        textViewSessionCode = findViewById(R.id.textViewSessionCode)
        textViewSessionCodeInstruction = findViewById(R.id.textViewSessionCodeInstruction)
        textViewTimer = findViewById(R.id.textViewTimer)
        textViewTimeUp = findViewById(R.id.textViewTimeUp)
        loadingView = findViewById(R.id.loadingView) // Cần thêm vào layout
        errorView = findViewById(R.id.errorView) // Cần thêm vào layout
        errorTextView = findViewById(R.id.errorTextView) // Cần thêm vào layout
        retryButton = findViewById(R.id.buttonRetry)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbarQRDisplay)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val courseName = intent.getStringExtra("COURSE_NAME")
        textViewCourseNameQR.text = courseName

        classId = intent.getIntExtra("CLASS_ID", -1)
        if (classId == -1) {
            Toast.makeText(this, "Lỗi: Không có ID lớp học.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        retryButton.setOnClickListener {
            viewModel.loadAttendanceData()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                loadingView.isVisible = state is UiState.Loading
                errorView.isVisible = state is UiState.Error
                imageViewQRCode.isVisible = state is UiState.Success
                textViewSessionCode.isVisible = state is UiState.Success
                textViewSessionCodeInstruction.isVisible = state is UiState.Success
                textViewTimer.isVisible = state is UiState.Success
                textViewTimeUp.isVisible = false

                when (state) {
                    is UiState.Loading -> { /* Đã xử lý isVisible */ }
                    is UiState.Success -> {
                        val (session, finalUrl) = state.data
                        textViewSessionCode.text = session.sessionCode
                        generateAndDisplayQRCode(finalUrl)
                        startCountdown(session)
                    }
                    is UiState.Error -> {
                        errorTextView.text = state.message
                    }
                    else -> { /* Không có trạng thái nào khác cần xử lý */ }
                }
            }
        }
    }

    private fun startCountdown(session: AttendanceSession) {
        countDownTimer?.cancel()

        val now = System.currentTimeMillis()
        val millisUntilFinished = session.endTime - now

        if (millisUntilFinished <= 0) {
            showTimeUp()
            return
        }

        countDownTimer = object : CountDownTimer(millisUntilFinished, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(minutes)
                textViewTimer.text = getString(R.string.qr_time_left, minutes, seconds)
            }

            override fun onFinish() {
                showTimeUp()
            }
        }.start()
    }

    private fun showTimeUp() {
        textViewTimer.text = getString(R.string.qr_time_up)
        imageViewQRCode.isVisible = false
        textViewSessionCode.isVisible = false
        textViewSessionCodeInstruction.isVisible = false
        textViewTimeUp.isVisible = true
    }

    private fun generateAndDisplayQRCode(content: String) {
        val writer = QRCodeWriter()
        try {
            // Mã hóa nội dung thành một BitMatrix
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 300, 300)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = createBitmap(width, height, Bitmap.Config.RGB_565)

            // Chuyển đổi BitMatrix thành Bitmap
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            imageViewQRCode.setImageBitmap(bmp)
        } catch (e: WriterException) {
            e.printStackTrace()
            Toast.makeText(this, "Không thể tạo mã QR.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel() // Rất quan trọng: Hủy timer để tránh rò rỉ bộ nhớ
    }
}
