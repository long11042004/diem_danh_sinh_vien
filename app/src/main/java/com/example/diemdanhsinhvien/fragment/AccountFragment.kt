package com.example.diemdanhsinhvien.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.activity.LoginActivity
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IPublicClientApplication
import android.provider.MediaStore
import com.example.diemdanhsinhvien.data.model.Account
import android.net.Uri
import android.content.pm.PackageManager
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.network.APIClient
import com.example.diemdanhsinhvien.repository.AccountRepository
import com.example.diemdanhsinhvien.viewmodel.AuthViewModel
import com.example.diemdanhsinhvien.viewmodel.AuthViewModelFactory
import com.example.diemdanhsinhvien.activity.EditAccountActivity
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import java.io.IOException
import com.microsoft.identity.client.exception.MsalException

class AccountFragment : Fragment() {

    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var imageViewAvatar: ImageView
    private val PICK_IMAGE_REQUEST = 123
    private lateinit var userIdTextView: TextView
    private lateinit var editAccountButton: Button
    private val EDIT_ACCOUNT_REQUEST_CODE = 124 

    private val authViewModel: AuthViewModel by viewModels { 
        AuthViewModelFactory(AccountRepository(
            accountApi = APIClient.accountApi
        ))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("AccountFragment", "onCreateView called")
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("AccountFragment", "onViewCreated called")

        initViews(view)
        setupObservers()
        setupClickListeners(view)

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

    private fun initViews(view: View) {
        userNameTextView = view.findViewById(R.id.textViewUserName)
        userEmailTextView = view.findViewById(R.id.textViewUserEmail)
        imageViewAvatar = view.findViewById(R.id.imageViewAvatar)
        userIdTextView = view.findViewById(R.id.textViewUserId)
        editAccountButton = view.findViewById(R.id.buttonEditAccount)
    }

    private fun setupClickListeners(view: View) {
        imageViewAvatar.setOnClickListener {
            Log.d("AccountFragment", "Avatar clicked, launching image picker.")
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        editAccountButton.setOnClickListener {
            Log.d("AccountFragment", "Edit Account button clicked.")
            val intent = Intent(requireActivity(), EditAccountActivity::class.java) 
            startActivityForResult(intent, EDIT_ACCOUNT_REQUEST_CODE)
        }
        val logoutButton = view.findViewById<Button>(R.id.buttonLogout)
        logoutButton.setOnClickListener {
            Log.d("AccountFragment", "Logout button clicked.")
            // Thử đăng xuất khỏi MSAL trước
            mSingleAccountApp?.getCurrentAccountAsync(object : ISingleAccountPublicClientApplication.CurrentAccountCallback {
                override fun onAccountLoaded(activeAccount: IAccount?) {
                    if (activeAccount != null) {
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
                    } else {
                        Log.i("AccountFragment", "No MSAL account found. Performing standard logout.")
                        navigateToLogin()
                    }
                }
                override fun onError(exception: MsalException) { Log.e("AccountFragment", "Error getting MSAL account for logout: $exception"); navigateToLogin() }
                override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {}
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
                    imageViewAvatar.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    Log.e("AccountFragment", "Error loading bitmap from URI.", e)
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updateUiWithAccountDetails(account: Account) {
        Log.d("AccountFragment", "Updating UI with account details: Name=${account.fullName}, Email=${account.email}, TeacherID=${account.teacherId}")
        userNameTextView.text = account.fullName
        userEmailTextView.text = account.email
        userIdTextView.text = "Mã GV: ${account.teacherId}" 
    }

    private fun updateUiAsLoggedOut() {
        Log.d("AccountFragment", "Updating UI to logged out state.")
        userNameTextView.text = getString(R.string.not_logged_in)
        userEmailTextView.text = ""
        userIdTextView.text = ""
    }

    private fun navigateToLogin() {
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
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