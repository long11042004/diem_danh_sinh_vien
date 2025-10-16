package com.example.diemdanhsinhvien.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.diemdanhsinhvien.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.request.RegisterRequest
import com.example.diemdanhsinhvien.network.apiservice.APIClient
import com.example.diemdanhsinhvien.repository.AccountRepository
import com.example.diemdanhsinhvien.viewmodel.AuthViewModel
import com.example.diemdanhsinhvien.viewmodel.AuthViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private val CONFIRM_PASSWORD_REQUEST_CODE = 123
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AccountRepository(APIClient.accountApi(applicationContext)))
    }

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

        observeViewModel()

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
                val fullName = findViewById<TextInputLayout>(R.id.fullNameTextInputLayout).editText?.text.toString().trim()
                val loginName = findViewById<TextInputLayout>(R.id.usernameTextInputLayout).editText?.text.toString().trim()
                val department = findViewById<TextInputLayout>(R.id.departmentTextInputLayout).editText?.text.toString().trim()
                val title = findViewById<TextInputLayout>(R.id.titleTextInputLayout).editText?.text.toString().trim()
                val phoneNumber = findViewById<TextInputLayout>(R.id.phoneNumberTextInputLayout).editText?.text.toString().trim()
                val dateOfBirthDisplay = findViewById<TextInputLayout>(R.id.dobTextInputLayout).editText?.text.toString().trim()
                val email = findViewById<TextInputLayout>(R.id.emailTextInputLayout).editText?.text.toString().trim()

                val dateOfBirthApi = try {
                    val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val date = displayFormat.parse(dateOfBirthDisplay)
                    date?.let { apiFormat.format(it) } ?: ""
                } catch (e: Exception) {
                    "" // Xử lý lỗi nếu định dạng không đúng
                }

                val registerRequest = RegisterRequest(
                    fullName = fullName,
                    loginName = loginName,
                    password = password,
                    department = department,
                    title = title,
                    phoneNumber = phoneNumber,
                    dateOfBirth = dateOfBirthApi,
                    email = email
                )
                authViewModel.register(registerRequest)
            }
        }
    }

    private fun observeViewModel() {
        val registerProgressBar = findViewById<ProgressBar>(R.id.registerProgressBar)
        val registerButton = findViewById<Button>(R.id.registerButton)

        authViewModel.registerResult.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    registerProgressBar.isVisible = true
                    registerButton.isEnabled = false
                }
                is UiState.Success -> {
                    registerProgressBar.isVisible = false
                    registerButton.isEnabled = true
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                is UiState.Error -> {
                    registerProgressBar.isVisible = false
                    registerButton.isEnabled = true
                    AlertDialog.Builder(this)
                        .setTitle("Lỗi đăng ký")
                        .setMessage(state.message)
                        .setPositiveButton("OK", null)
                        .show()
                }
                else -> {
                    registerProgressBar.isVisible = false
                    registerButton.isEnabled = true
                }
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

    private fun validateAndRegister() {
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

        if (isFormValid) {
            val intent = Intent(this, ConfirmPasswordActivity::class.java)
            startActivityForResult(intent, CONFIRM_PASSWORD_REQUEST_CODE)
        }
    }
}