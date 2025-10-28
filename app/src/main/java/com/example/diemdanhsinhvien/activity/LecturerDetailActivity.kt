package com.example.diemdanhsinhvien.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.model.Account
import com.example.diemdanhsinhvien.network.apiservice.APIClient
import com.example.diemdanhsinhvien.repository.AccountRepository
import com.example.diemdanhsinhvien.viewmodel.AuthViewModel
import com.example.diemdanhsinhvien.viewmodel.AuthViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale

class LecturerDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_LECTURER_ID = "extra_lecturer_id"
    }

    private lateinit var ivAvatar: ImageView
    private lateinit var tvFullName: TextView
    private lateinit var tvLecturerId: TextView
    private lateinit var tvDepartment: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvEmailValue: TextView
    private lateinit var tvPhoneValue: TextView
    private lateinit var tvDobValue: TextView
    private lateinit var tvCreatedAt: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var contentGroup: Group

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            AccountRepository(
                accountApi = APIClient.accountApi(this)
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_account)

        val lecturerId = intent.getIntExtra(EXTRA_LECTURER_ID, -1)
        if (lecturerId == -1) {
            Toast.makeText(this, "ID giảng viên không hợp lệ.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        hideActionButtons()
        setupObservers()

        authViewModel.getAccountById(lecturerId)
    }

    private fun setupViews() {
        ivAvatar = findViewById(R.id.iv_avatar)
        tvFullName = findViewById(R.id.tv_full_name)
        tvLecturerId = findViewById(R.id.tv_lecturer_id_value)
        tvDepartment = findViewById(R.id.tv_department_value)
        tvTitle = findViewById(R.id.tv_title_value)
        tvEmailValue = findViewById(R.id.tv_email_value)
        tvPhoneValue = findViewById(R.id.tv_phone_value)
        tvDobValue = findViewById(R.id.tv_dob_value)
        tvCreatedAt = findViewById(R.id.tv_created_at)
        progressBar = findViewById(R.id.progress_bar_account)
        contentGroup = findViewById(R.id.group_account_content)

        ivAvatar.isClickable = false
    }

    private fun hideActionButtons() {
        findViewById<Button>(R.id.btn_edit_account).visibility = View.GONE
        findViewById<Button>(R.id.btn_change_password).visibility = View.GONE
        findViewById<Button>(R.id.btn_logout).visibility = View.GONE
    }

    private fun setupObservers() {
        authViewModel.accountById.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    contentGroup.visibility = View.GONE
                }
                is UiState.Success -> {
                    progressBar.visibility = View.GONE
                    contentGroup.visibility = View.VISIBLE
                    state.data?.let { account ->
                        updateUiWithAccountDetails(account)
                    }
                }
                is UiState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Lỗi khi tải thông tin: ${state.message}", Toast.LENGTH_LONG).show()
                    finish() // Đóng Activity nếu không tải được dữ liệu
                }
                else -> {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun updateUiWithAccountDetails(account: Account) {
        tvFullName.text = account.fullName
        tvLecturerId.text = account.teacherId
        tvDepartment.text = account.department ?: "Chưa cập nhật"
        tvTitle.text = account.title ?: "Chưa cập nhật"
        tvEmailValue.text = account.email
        tvPhoneValue.text = account.phoneNumber
        tvDobValue.text = account.dateOfBirth?.let { formatDate(it) }

        account.created_at?.let {
            val formattedDate = formatDate(it)
            tvCreatedAt.text = "Ngày tạo: $formattedDate"
            tvCreatedAt.visibility = View.VISIBLE
        } ?: run {
            tvCreatedAt.visibility = View.GONE
        }
    }

    private fun formatDate(dateString: String): String {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val date = parser.parse(dateString)
            if (date != null) formatter.format(date) else dateString
        } catch (e: Exception) {
            val simpleParser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try { formatter.format(simpleParser.parse(dateString)!!) } catch (e2: Exception) { dateString }
        }
    }
}
