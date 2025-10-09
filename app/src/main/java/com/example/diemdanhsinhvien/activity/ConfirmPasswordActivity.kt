package com.example.diemdanhsinhvien.activity

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.diemdanhsinhvien.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputLayout

class ConfirmPasswordActivity : AppCompatActivity() {
    private var otpCode: String? = null
    private var confirmedPassword: String? = null
    private val OTP_REQUEST_CODE = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_password)

        val toolbar = findViewById<MaterialToolbar>(R.id.confirmPasswordToolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }


        val confirmButton = findViewById<Button>(R.id.confirmButton)
        confirmButton.setOnClickListener {
            val passwordLayout = findViewById<TextInputLayout>(R.id.passwordTextInputLayout)
            val confirmPasswordLayout = findViewById<TextInputLayout>(R.id.confirmPasswordTextInputLayout)

            val password = passwordLayout.editText?.text.toString()
            val confirmPassword = confirmPasswordLayout.editText?.text.toString()

            val fullName = intent.getStringExtra("fullName")
            val username = intent.getStringExtra("username")
            val school = intent.getStringExtra("school")
            val phone = intent.getStringExtra("phone")
            val dob = intent.getStringExtra("dob")
            val email = intent.getStringExtra("email")
            val department = intent.getStringExtra("department")
            val title = intent.getStringExtra("title")


            if (password.isEmpty()) {
                passwordLayout.error = getString(R.string.error_enter_password)
            } else {
                passwordLayout.error = null
            }

            if (confirmPassword.isEmpty()) {
                confirmPasswordLayout.error = getString(R.string.error_enter_confirm_password)
            } else {
                confirmPasswordLayout.error = null
            }

             if (password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                 if (password == confirmPassword) {
                    this.confirmedPassword = password
                     val intent = Intent(this, OtpActivity::class.java)
                     intent.putExtra("fullName", fullName)
                     intent.putExtra("username", username)
                     intent.putExtra("school", school)
                     intent.putExtra("phone", phone)
                     intent.putExtra("dob", dob)
                     intent.putExtra("email", email)
                     intent.putExtra("department", department)
                     intent.putExtra("title", title)

                     otpCode = generateOtp()
                     sendOtp(otpCode)
                     startActivityForResult(intent, OTP_REQUEST_CODE)
                 } else {
                     confirmPasswordLayout.error = getString(R.string.error_password_mismatch)
                 }
             }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OTP_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val resultIntent = Intent()
                resultIntent.putExtra("password", confirmedPassword)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun generateOtp(): String {
        return (100000..999999).random().toString()
    }

    private fun sendOtp(otp: String?) {
        Toast.makeText(this, "OTP code: $otp", Toast.LENGTH_LONG).show()
    }

}
