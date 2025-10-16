package com.example.diemdanhsinhvien.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Patterns
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.model.Account
import com.example.diemdanhsinhvien.network.apiservice.APIClient
import com.example.diemdanhsinhvien.repository.AccountRepository
import com.example.diemdanhsinhvien.viewmodel.AuthViewModel
import com.example.diemdanhsinhvien.viewmodel.AuthViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditAccountActivity : AppCompatActivity() {

    // Views
    private lateinit var toolbar: MaterialToolbar
    private lateinit var avatarImageView: ImageView
    private lateinit var fullNameEditText: TextInputEditText
    private lateinit var departmentEditText: TextInputEditText
    private lateinit var titleEditText: TextInputEditText
    private lateinit var phoneNumberEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var dobEditText: TextInputEditText
    private lateinit var dobInputLayout: TextInputLayout
    private lateinit var saveButton: Button
    private lateinit var progressBar: ProgressBar

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AccountRepository(APIClient.accountApi(this)))
    }

    private var currentAccount: Account? = null
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_account)

        initViews()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupObservers()
        setupClickListeners()

        authViewModel.getAccountDetails()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbarEditAccount)
        avatarImageView = findViewById(R.id.iv_edit_avatar)
        fullNameEditText = findViewById(R.id.editTextFullName)
        departmentEditText = findViewById(R.id.editTextDepartment)
        titleEditText = findViewById(R.id.editTextTitle)
        phoneNumberEditText = findViewById(R.id.editTextPhoneNumber)
        emailEditText = findViewById(R.id.editTextEmail)
        dobEditText = findViewById(R.id.editTextDob)
        dobInputLayout = findViewById(R.id.textInputDob)
        saveButton = findViewById(R.id.buttonSave)
        progressBar = findViewById(R.id.progressBarEdit)
    }

    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            saveChanges()
        }

        dobEditText.setOnClickListener {
            showDatePickerDialog()
        }
        dobInputLayout.setEndIconOnClickListener {
            showDatePickerDialog()
        }

        avatarImageView.setOnClickListener {
            Toast.makeText(this, "Chức năng thay đổi ảnh đại diện đang được phát triển", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        // Lắng nghe kết quả tải dữ liệu ban đầu
        authViewModel.accountDetails.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    saveButton.isEnabled = false
                }
                is UiState.Success -> {
                    progressBar.visibility = View.GONE
                    saveButton.isEnabled = true
                    state.data?.let {
                        currentAccount = it
                        populateData(it)
                    }
                }
                is UiState.Error -> {
                    progressBar.visibility = View.GONE
                    saveButton.isEnabled = true
                    Toast.makeText(this, "Lỗi tải dữ liệu: ${state.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        // Lắng nghe kết quả của việc cập nhật
        authViewModel.updateState.observe(this) { state -> // Giả định có updateState trong ViewModel
            when (state) {
                is UiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    saveButton.isEnabled = false
                }
                is UiState.Success -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_LONG).show()
                    // Báo kết quả thành công về cho AccountFragment và đóng màn hình
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                is UiState.Error -> {
                    progressBar.visibility = View.GONE
                    saveButton.isEnabled = true
                    Toast.makeText(this, "Cập nhật thất bại: ${state.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun populateData(account: Account) {
        fullNameEditText.setText(account.fullName)
        departmentEditText.setText(account.department)
        titleEditText.setText(account.title)
        phoneNumberEditText.setText(account.phoneNumber)
        emailEditText.setText(account.email)

        // Định dạng và hiển thị ngày sinh
        account.dateOfBirth?.let {
            dobEditText.setText(formatDateForDisplay(it))
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = inputFormat.parse(it)
                if (date != null) {
                    calendar.time = date
                }
                else {}
            } catch (e: Exception) {
                Log.e("EditAccountActivity", "Lỗi parse ngày sinh: $it")
            }
        }

        /* Glide.with(this)
            .load(account.avatarUrl)
            .placeholder(R.drawable.ic_account)
            .error(R.drawable.ic_account)
            .circleCrop()
            .into(avatarImageView) */
    }

    private fun saveChanges() {
        val localCurrentAccount = currentAccount ?: run {
            Toast.makeText(this, "Dữ liệu người dùng chưa sẵn sàng, vui lòng thử lại.", Toast.LENGTH_SHORT).show()
            return
        }

        val fullName = fullNameEditText.text.toString().trim()
        if (fullName.isEmpty()) {
            fullNameEditText.error = getString(R.string.error_enter_full_name)
            return
        }

        val email = emailEditText.text.toString().trim()
        if (email.isEmpty()) {
            emailEditText.error = getString(R.string.error_enter_email)
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = getString(R.string.error_invalid_email)
            return
        }


        val updatedAccount = localCurrentAccount.copy(
            fullName = fullName,
            email = email,
            department = departmentEditText.text.toString().trim(),
            title = titleEditText.text.toString().trim(),
            phoneNumber = phoneNumberEditText.text.toString().trim(),
            dateOfBirth = if (dobEditText.text.toString().isNotEmpty()) {
                formatDateForApi(calendar.time)
            } else {
                ""
            }
        )

        authViewModel.updateAccount(updatedAccount.id, updatedAccount)
    }

    private fun showDatePickerDialog() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dobEditText.setText(displayFormat.format(calendar.time))
        }

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun formatDateForDisplay(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            if (date != null) outputFormat.format(date) else ""
        } catch (e: Exception) {
            dateString
        }
    }

    private fun formatDateForApi(date: Date): String {
        val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return apiFormat.format(date)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
