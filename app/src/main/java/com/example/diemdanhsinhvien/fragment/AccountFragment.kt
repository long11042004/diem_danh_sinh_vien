package com.example.diemdanhsinhvien.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.app.Activity
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import com.example.diemdanhsinhvien.R
import android.widget.ImageView
import android.widget.Button
import android.widget.TextView
import com.example.diemdanhsinhvien.activity.LoginActivity
import com.example.diemdanhsinhvien.manager.SessionManager
import android.provider.MediaStore
import com.example.diemdanhsinhvien.data.model.Account
import android.net.Uri
import com.bumptech.glide.Glide
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.network.apiservice.APIClient
import com.example.diemdanhsinhvien.repository.AccountRepository
import com.example.diemdanhsinhvien.viewmodel.AuthViewModel
import com.example.diemdanhsinhvien.viewmodel.AuthViewModelFactory
import com.example.diemdanhsinhvien.activity.EditAccountActivity
import com.example.diemdanhsinhvien.activity.ChangePasswordActivity
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import java.io.IOException
import com.microsoft.identity.client.exception.MsalException
import java.text.SimpleDateFormat
import java.util.Locale

class AccountFragment : Fragment() {

    private lateinit var ivAvatar: ImageView
    private lateinit var tvFullName: TextView
    private lateinit var tvLecturerId: TextView
    private lateinit var tvDepartment: TextView
    private lateinit var tvTitle: TextView
    private lateinit var btnEditAccount: Button
    private lateinit var btnChangePassword: Button
    private lateinit var btnLogout: Button
    private lateinit var tvEmailValue: TextView
    private lateinit var tvPhoneValue: TextView
    private lateinit var tvDobValue: TextView
    private lateinit var tvCreatedAt: TextView

    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
    private val PICK_IMAGE_REQUEST = 123
    private val EDIT_ACCOUNT_REQUEST_CODE = 124
    private lateinit var sessionManager: SessionManager

    private val authViewModel: AuthViewModel by viewModels { 
        AuthViewModelFactory(AccountRepository(
            accountApi = APIClient.accountApi(requireContext())
        ))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        ivAvatar = view.findViewById(R.id.iv_avatar)
        tvFullName = view.findViewById(R.id.tv_full_name)
        tvLecturerId = view.findViewById(R.id.tv_lecturer_id_value)
        tvDepartment = view.findViewById(R.id.tv_department_value)
        tvTitle = view.findViewById(R.id.tv_title_value)
        btnEditAccount = view.findViewById(R.id.btn_edit_account)
        btnChangePassword = view.findViewById(R.id.btn_change_password)
        btnLogout = view.findViewById(R.id.btn_logout)
        tvEmailValue = view.findViewById(R.id.tv_email_value)
        tvPhoneValue = view.findViewById(R.id.tv_phone_value)
        tvDobValue = view.findViewById(R.id.tv_dob_value)
        tvCreatedAt = view.findViewById(R.id.tv_created_at)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("AccountFragment", "onViewCreated called")

        sessionManager = SessionManager(requireContext())
        setupObservers()
        setupClickListeners()

        authViewModel.getAccountDetails()

        PublicClientApplication.createSingleAccountPublicClientApplication(requireContext(),
            R.raw.auth_config_single_account,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    Log.i("AccountFragment", "MSAL PublicClientApplication created successfully.")
                    mSingleAccountApp = application
                }
                override fun onError(exception: MsalException) {
                    Log.e("AccountFragment", "Error creating MSAL application: $exception")
                }
            })
    }

    private fun setupClickListeners() {
        ivAvatar.setOnClickListener {
            Log.d("AccountFragment", "Avatar clicked, launching image picker.")
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnEditAccount.setOnClickListener {
            Log.d("AccountFragment", "Edit Account button clicked.")
            val intent = Intent(requireActivity(), EditAccountActivity::class.java)
            startActivityForResult(intent, EDIT_ACCOUNT_REQUEST_CODE)
        }

        btnChangePassword.setOnClickListener {
            Log.d("AccountFragment", "Change Password button clicked.")
            val intent = Intent(requireActivity(), ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            Log.d("AccountFragment", "Logout button clicked.")
            sessionManager.clearTokens()
            Log.i("AccountFragment", "Custom session tokens cleared.")

            if (mSingleAccountApp == null) {
                Log.w("AccountFragment", "MSAL app not initialized. Proceeding with standard logout.")
                navigateToLogin()
                return@setOnClickListener
            }

            mSingleAccountApp?.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
                override fun onSignOut() {
                    Log.i("AccountFragment", "MSAL sign out successful.")
                    navigateToLogin()
                }
                override fun onError(exception: MsalException) {
                    Log.e("AccountFragment", "MSAL sign out error: $exception")
                    navigateToLogin()
                }
            })
        }
    }

    private fun setupObservers() {
        authViewModel.accountDetails.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    Log.d("AccountFragment", "Đang tải chi tiết tài khoản...")
                }
                is UiState.Success -> {
                    Log.i("AccountFragment", "Successfully loaded account details.")
                    state.data?.let { account ->
                        updateUiWithAccountDetails(account)
                    } ?: run {
                        Log.w("AccountFragment", "Account details are null, updating UI to logged out.")
                        updateUiAsLoggedOut()
                    }
                }
                is UiState.Error -> {
                    Log.e("AccountFragment", "Lỗi khi tải chi tiết tài khoản: ${state.message}")
                    Toast.makeText(requireContext(), "Lỗi khi tải thông tin chi tiết.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Log.w("AccountFragment", "Observed an unhandled or null state for accountDetails.")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("AccountFragment", "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        if (requestCode == EDIT_ACCOUNT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Log.i("AccountFragment", "Returned from EditAccountActivity with RESULT_OK. Refreshing account details.")
            authViewModel.getAccountDetails()
            Toast.makeText(requireContext(), "Đã cập nhật thông tin tài khoản.", Toast.LENGTH_SHORT).show()
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            if (selectedImageUri != null) {
                Log.i("AccountFragment", "Image selected from gallery: $selectedImageUri")
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedImageUri)
                    ivAvatar.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    Log.e("AccountFragment", "Error loading bitmap from URI.", e)
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updateUiWithAccountDetails(account: Account) {
        Log.d("AccountFragment", "Updating UI with account details: $account")
        tvFullName.text = account.fullName

        tvLecturerId.text = account.teacherId
        tvDepartment.text = account.department ?: "Chưa cập nhật"
        tvTitle.text = account.title ?: "Chưa cập nhật"

        tvEmailValue.text = account.email
        tvPhoneValue.text = account.phoneNumber
        tvDobValue.text = account.dateOfBirth?.let { formatDate(it) }

        account.created_at?.let {
            val formattedDate = formatDate(it)
            tvCreatedAt.text = "${getString(R.string.created_at_label)}: $formattedDate"
            tvCreatedAt.visibility = View.VISIBLE
        } ?: run {
            tvCreatedAt.visibility = View.GONE
        }

        // Sử dụng Glide để tải ảnh đại diện
        /* Glide.with(this)
            .load(account.avatarUrl)
            .placeholder(R.drawable.ic_account) // Ảnh mặc định
            .error(R.drawable.ic_account) // Ảnh khi có lỗi
            .circleCrop() // Bo tròn ảnh
            .into(ivAvatar) */
    }

    private fun updateUiAsLoggedOut() {
        tvFullName.text = getString(R.string.not_logged_in)
        tvLecturerId.text = ""
        tvDepartment.text = ""
        tvTitle.text = ""
        ivAvatar.setImageResource(R.drawable.ic_account) // Reset avatar to default
        tvEmailValue.text = ""
        tvPhoneValue.text = ""
        tvDobValue.text = ""
        tvCreatedAt.text = ""
    }

    private fun navigateToLogin() {
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun formatDate(dateString: String): String {
        // Các định dạng ngày tháng có thể nhận từ API
        val inputFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        )
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        for (format in inputFormats) {
            try {
                val date = format.parse(dateString)
                if (date != null) {
                    return outputFormat.format(date)
                }
            } catch (e: Exception) {
                // Bỏ qua và thử định dạng tiếp theo
            }
        }
        Log.w("AccountFragment", "Không thể phân tích ngày: $dateString. Trả về chuỗi gốc.")
        return dateString // Trả về chuỗi gốc nếu không thể phân tích
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("AccountFragment", "onRequestPermissionsResult: requestCode=$requestCode")
        when (requestCode) {
            PICK_IMAGE_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("AccountFragment", "Storage permission granted for image picking.")
                } else {
                    Log.w("AccountFragment", "Storage permission denied for image picking.")
                }
            }
        }
    }
}