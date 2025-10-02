package com.example.diemdanhsinhvien.activity

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.diemdanhsinhvien.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        val toolbar = findViewById<MaterialToolbar>(R.id.registerToolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val dobEditText = findViewById<TextInputEditText>(R.id.dobEditText)
        dobEditText.setOnClickListener {
            showDatePicker()
        }

        val registerButton = findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener {
            validateAndRegister()
        }

        val loginNowTextView = findViewById<TextView>(R.id.textViewLoginNow)
        loginNowTextView.setOnClickListener {
            finish() // Đóng màn hình đăng ký để quay lại màn hình đăng nhập
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Chọn ngày sinh")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val formattedDate = sdf.format(selection)
            findViewById<TextInputEditText>(R.id.dobEditText).setText(formattedDate)
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun validateAndRegister() {
        val fullNameLayout = findViewById<TextInputLayout>(R.id.fullNameTextInputLayout)
        val schoolLayout = findViewById<TextInputLayout>(R.id.schoolTextInputLayout)
        val phoneLayout = findViewById<TextInputLayout>(R.id.phoneNumberTextInputLayout)
        val dobLayout = findViewById<TextInputLayout>(R.id.dobTextInputLayout)
        val emailLayout = findViewById<TextInputLayout>(R.id.emailTextInputLayout)
        val passwordLayout = findViewById<TextInputLayout>(R.id.passwordTextInputLayout)
        val confirmPasswordLayout = findViewById<TextInputLayout>(R.id.confirmPasswordTextInputLayout)

        val fullName = fullNameLayout.editText?.text.toString().trim()
        val school = schoolLayout.editText?.text.toString().trim()
        val phone = phoneLayout.editText?.text.toString().trim()
        val dob = dobLayout.editText?.text.toString().trim()
        val email = emailLayout.editText?.text.toString().trim()
        val password = passwordLayout.editText?.text.toString()
        val confirmPassword = confirmPasswordLayout.editText?.text.toString()

        var isFormValid = true

        // Reset errors
        fullNameLayout.error = null
        schoolLayout.error = null
        phoneLayout.error = null
        dobLayout.error = null
        emailLayout.error = null
        passwordLayout.error = null
        confirmPasswordLayout.error = null

        // Validate all fields
        if (fullName.isEmpty()) {
            fullNameLayout.error = getString(R.string.error_enter_full_name)
            isFormValid = false
        }
        if (school.isEmpty()) {
            schoolLayout.error = getString(R.string.error_enter_school)
            isFormValid = false
        }
        if (phone.isEmpty()) {
            phoneLayout.error = getString(R.string.error_enter_phone)
            isFormValid = false
        }
        if (email.isEmpty()) {
            emailLayout.error = getString(R.string.error_enter_email)
            isFormValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = getString(R.string.error_invalid_email)
            isFormValid = false
        }
        if (password.isEmpty()) {
            passwordLayout.error = getString(R.string.error_enter_password)
            isFormValid = false
        } else if (password.length < 6) {
            passwordLayout.error = getString(R.string.error_password_length)
            isFormValid = false
        }
        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.error = getString(R.string.error_confirm_password)
            isFormValid = false
        } else if (password != confirmPassword) {
            confirmPasswordLayout.error = getString(R.string.error_password_mismatch)
            isFormValid = false
        }

        if (isFormValid) {
            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
            // TODO: Implement actual registration logic (e.g., API call)
            finish()
        }
    }
}