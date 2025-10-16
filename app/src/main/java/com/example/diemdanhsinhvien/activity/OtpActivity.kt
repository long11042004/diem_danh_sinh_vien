package com.example.diemdanhsinhvien.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.diemdanhsinhvien.R
import com.google.android.material.appbar.MaterialToolbar

class OtpActivity : AppCompatActivity() {

    private lateinit var otpDigit1: EditText
    private lateinit var otpDigit2: EditText
    private lateinit var otpDigit3: EditText
    private lateinit var otpDigit4: EditText
    private lateinit var otpDigit5: EditText
    private lateinit var otpDigit6: EditText
    private lateinit var verifyOtpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        val toolbar = findViewById<MaterialToolbar>(R.id.otpToolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        otpDigit1 = findViewById(R.id.otpDigit1)
        otpDigit2 = findViewById(R.id.otpDigit2)
        otpDigit3 = findViewById(R.id.otpDigit3)
        otpDigit4 = findViewById(R.id.otpDigit4)
        otpDigit5 = findViewById(R.id.otpDigit5)
        otpDigit6 = findViewById(R.id.otpDigit6)
        verifyOtpButton = findViewById(R.id.verifyOtpButton)

        setupOtpListeners()

        verifyOtpButton.setOnClickListener {
            val otp = getOtpCode()
            if (otp.length == 6) {
                Toast.makeText(this, "Mã OTP đã nhập: $otp", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Vui lòng nhập đủ 6 chữ số OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupOtpListeners() {
        val otpEditTexts = arrayOf(otpDigit1, otpDigit2, otpDigit3, otpDigit4, otpDigit5, otpDigit6)

        for (i in otpEditTexts.indices) {
            val currentEditText = otpEditTexts[i]
            val nextEditText = if (i < otpEditTexts.size - 1) otpEditTexts[i + 1] else null
            val previousEditText = if (i > 0) otpEditTexts[i - 1] else null

            currentEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        nextEditText?.requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // Do nothing
                }
            })

            currentEditText.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (currentEditText.text.isEmpty() && previousEditText != null) {
                        previousEditText.requestFocus()
                        previousEditText.setText("")
                        return@OnKeyListener true
                    }
                }
                false
            })
        }
    }

    private fun getOtpCode(): String {
        return "${otpDigit1.text}${otpDigit2.text}${otpDigit3.text}${otpDigit4.text}${otpDigit5.text}${otpDigit6.text}"
    }
}