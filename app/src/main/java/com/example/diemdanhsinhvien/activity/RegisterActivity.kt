package com.example.diemdanhsinhvien.activity

import android.app.Activity
import android.content.Intent
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
import java.util.UUID

class RegisterActivity : AppCompatActivity() {

    private val CONFIRM_PASSWORD_REQUEST_CODE = 123
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
            if (validateAndRegister()) {
                val intent = Intent(this, ConfirmPasswordActivity::class.java)
                startActivityForResult(intent, CONFIRM_PASSWORD_REQUEST_CODE)
            }
        }

        val loginNowTextView = findViewById<TextView>(R.id.textViewLoginNow)
        loginNowTextView.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CONFIRM_PASSWORD_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val password = data?.getStringExtra("password")
            if (password != null) {
                val fullName = intent.getStringExtra("fullName")
                val username = intent.getStringExtra("username")
                val school = intent.getStringExtra("school")
                val phone = intent.getStringExtra("phone")
                val dob = intent.getStringExtra("dob")
                val email = intent.getStringExtra("email")
                val department = intent.getStringExtra("department")
                val title = intent.getStringExtra("title")

                Toast.makeText(this, "Đăng ký và đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
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

    private fun generateTeacherId(): String {
        val uuid = UUID.randomUUID().toString().substring(0, 8).uppercase()
        return "GV$uuid"
    }

    private fun validateAndRegister(): Boolean {
        val fullNameLayout = findViewById<TextInputLayout>(R.id.fullNameTextInputLayout)
        val usernameLayout = findViewById<TextInputLayout>(R.id.usernameTextInputLayout)
        val departmentLayout = findViewById<TextInputLayout>(R.id.departmentTextInputLayout)
        val titleLayout = findViewById<TextInputLayout>(R.id.titleTextInputLayout)
        val phoneLayout = findViewById<TextInputLayout>(R.id.phoneNumberTextInputLayout)
        val dobLayout = findViewById<TextInputLayout>(R.id.dobTextInputLayout)
        val emailLayout = findViewById<TextInputLayout>(R.id.emailTextInputLayout)
        
        val fullName = fullNameLayout.editText?.text.toString().trim()
        val username = usernameLayout.editText?.text.toString().trim()

        val department = departmentLayout.editText?.text.toString().trim()
        val title = titleLayout.editText?.text.toString().trim()
       
        val phone = phoneLayout.editText?.text.toString().trim()
        val dob = dobLayout.editText?.text.toString().trim()
        val email = emailLayout.editText?.text.toString().trim()
        
        var isFormValid = true
        
        fullNameLayout.error = null
      
        phoneLayout.error = null
        dobLayout.error = null
        emailLayout.error = null

        if (fullName.isEmpty()) {
            fullNameLayout.error = getString(R.string.error_enter_full_name)
            isFormValid = false
        }

        if (username.isEmpty()) {
            usernameLayout.error = getString(R.string.error_enter_username)
            isFormValid = false
        }

        if (department.isEmpty()) {
            departmentLayout.error = "Vui lòng nhập khoa"
            isFormValid = false
        }
        if (title.isEmpty()) {
            titleLayout.error = "Vui lòng nhập title"
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

        return isFormValid
    }
}