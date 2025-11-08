package com.example.diemdanhsinhvien.activity

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.android.material.appbar.MaterialToolbar
import com.example.diemdanhsinhvien.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

class QRDisplayActivity : AppCompatActivity() {

    private lateinit var imageViewQRCode: ImageView
    private lateinit var textViewCourseNameQR: TextView
    private lateinit var textViewSessionCode: TextView
    private lateinit var textViewSessionCodeInstruction: TextView

    private val msFormPrefillUrl = "https://forms.office.com/r/R0hD5vRwSH"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_display)

        imageViewQRCode = findViewById(R.id.imageViewQRCode)
        textViewCourseNameQR = findViewById(R.id.textViewCourseNameQR)
        textViewSessionCode = findViewById(R.id.textViewSessionCode)
        textViewSessionCodeInstruction = findViewById(R.id.textViewSessionCodeInstruction)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbarQRDisplay)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val courseName = intent.getStringExtra("COURSE_NAME")
        textViewCourseNameQR.text = courseName

        textViewSessionCode.visibility = View.GONE
        textViewSessionCodeInstruction.visibility = View.GONE

        // Tạo mã QR trực tiếp từ link cố định
        generateAndDisplayQRCode(msFormPrefillUrl)
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
}
