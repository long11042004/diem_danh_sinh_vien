package com.example.diemdanhsinhvien.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.diemdanhsinhvien.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputLayout

class OtpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        val toolbar = findViewById<MaterialToolbar>(R.id.otpToolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val verifyButton = findViewById<Button>(R.id.verifyOtpButton)
        verifyButton.setOnClickListener {
            Toast.makeText(this, "OTP Verified", Toast.LENGTH_SHORT).show()
            val resultIntent = Intent()
            resultIntent.putExtra("otp_verified", true)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
