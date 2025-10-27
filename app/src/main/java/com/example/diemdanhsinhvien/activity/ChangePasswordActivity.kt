package com.example.diemdanhsinhvien.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.network.apiservice.APIClient
import com.example.diemdanhsinhvien.repository.AccountRepository
import com.example.diemdanhsinhvien.viewmodel.AuthViewModel
import com.example.diemdanhsinhvien.viewmodel.AuthViewModelFactory
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var tilOldPassword: TextInputLayout
    private lateinit var tilNewPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etOldPassword: TextInputEditText
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSaveChanges: Button
    private lateinit var btnBack: ImageButton
    private lateinit var tvErrorMessage: TextView

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AccountRepository(
            accountApi = APIClient.accountApi(this)
        ))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        btnBack = findViewById(R.id.btn_back)
        tilOldPassword = findViewById(R.id.til_old_password)
        tilNewPassword = findViewById(R.id.til_new_password)
        tilConfirmPassword = findViewById(R.id.til_confirm_password)
        etOldPassword = findViewById(R.id.et_old_password)
        etNewPassword = findViewById(R.id.et_new_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnSaveChanges = findViewById(R.id.btn_save_changes)
        tvErrorMessage = findViewById(R.id.tv_error_message)

        setupClickListeners()
        setupObservers()
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnSaveChanges.setOnClickListener {
            if (validateInput()) {
                val oldPassword = etOldPassword.text.toString().trim()
                val newPassword = etNewPassword.text.toString().trim()
                authViewModel.changePassword(oldPassword, newPassword)
            }
        }
    }

    private fun validateInput(): Boolean {
        tilOldPassword.error = null
        tilNewPassword.error = null
        tilConfirmPassword.error = null
        tvErrorMessage.visibility = View.GONE

        var isValid = true

        if (etOldPassword.text.toString().trim().isEmpty()) {
            tilOldPassword.error = "Vui lòng nhập mật khẩu cũ"
            isValid = false
        }

        if (etNewPassword.text.toString().trim().isEmpty()) {
            tilNewPassword.error = "Vui lòng nhập mật khẩu mới"
            isValid = false
        }

        if (etConfirmPassword.text.toString().trim().isEmpty()) {
            tilConfirmPassword.error = "Vui lòng xác nhận mật khẩu mới"
            isValid = false
        }

        if (!isValid) return false

        if (etNewPassword.text.toString() != etConfirmPassword.text.toString()) {
            tilConfirmPassword.error = "Mật khẩu xác nhận không khớp"
            isValid = false
        }

        return isValid
    }

    private fun setupObservers() {
        authViewModel.changePasswordState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    btnSaveChanges.isEnabled = false

                    tilOldPassword.error = null
                    tilNewPassword.error = null
                    tilConfirmPassword.error = null
                    tvErrorMessage.visibility = View.GONE
                }
                is UiState.Success -> {
                    btnSaveChanges.isEnabled = true
                    Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is UiState.Error -> {
                    btnSaveChanges.isEnabled = true

                    tvErrorMessage.text = state.message
                    tvErrorMessage.visibility = View.VISIBLE
                }
                else -> {}
            }
        }
    }
}
