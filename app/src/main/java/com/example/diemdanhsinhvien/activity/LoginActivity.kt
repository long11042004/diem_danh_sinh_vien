package com.example.diemdanhsinhvien.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.request.LoginRequest
import com.example.diemdanhsinhvien.network.apiservice.APIClient
import com.example.diemdanhsinhvien.manager.SessionManager
import com.example.diemdanhsinhvien.repository.AccountRepository
import com.example.diemdanhsinhvien.viewmodel.AuthViewModel
import com.example.diemdanhsinhvien.viewmodel.AuthViewModelFactory
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
    private lateinit var sessionManager: SessionManager

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AccountRepository(
            APIClient.accountApi(applicationContext)
        ))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.login_activity)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        PublicClientApplication.createSingleAccountPublicClientApplication(this,
            R.raw.auth_config_single_account,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    mSingleAccountApp = application
                    loadAccount()
                }
                override fun onError(exception: MsalException) {
                    Log.e("MSAL", "Lỗi khi khởi tạo MSAL: $exception")
                    Toast.makeText(this@LoginActivity, "Lỗi khởi tạo dịch vụ đăng nhập.", Toast.LENGTH_LONG).show()
                }
            })

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val usernameInputLayout = findViewById<TextInputLayout>(R.id.usernameTextInputLayout)
        val passwordInputLayout = findViewById<TextInputLayout>(R.id.passwordTextInputLayout)
        val usernameEditText = usernameInputLayout.editText
        val passwordEditText = passwordInputLayout.editText
        val forgotPasswordTextView = findViewById<TextView>(R.id.textViewForgotPassword)
        val registerTextView = findViewById<TextView>(R.id.textViewRegister)
        val microsoftSignInButton = findViewById<Button>(R.id.microsoftSignInButton)
        val errorTextView = findViewById<TextView>(R.id.textViewLoginError)

        val loginProgressBar = findViewById<ProgressBar>(R.id.loginProgressBar)

        loginButton.setOnClickListener {
            
            usernameInputLayout.error = null
            passwordInputLayout.error = null
            errorTextView.visibility = View.GONE

            val username = usernameEditText?.text.toString().trim()
            val password = passwordEditText?.text.toString()

            var isFormValid = true

            if (username.isEmpty()) {
                usernameInputLayout.error = getString(R.string.field_required_error)
                isFormValid = false
            }

            if (password.isEmpty()) {
                passwordInputLayout.error = getString(R.string.field_required_error)
                isFormValid = false
            }

            if (isFormValid) {
                val request = LoginRequest(loginName = username, password = password)
                authViewModel.login(request)
            }
        }

        forgotPasswordTextView.setOnClickListener {
            Toast.makeText(this, "Chức năng Quên mật khẩu đang được phát triển", Toast.LENGTH_SHORT).show()
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        microsoftSignInButton.setOnClickListener {
            signInWithMicrosoft()
        }
    }

    private fun observeViewModel() {
        val errorTextView = findViewById<TextView>(R.id.textViewLoginError)
        val loginProgressBar = findViewById<ProgressBar>(R.id.loginProgressBar)
        val loginButton = findViewById<Button>(R.id.buttonLogin)

        sessionManager = SessionManager(applicationContext)

        authViewModel.loginResult.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    loginProgressBar.isVisible = true
                    loginButton.isEnabled = false
                    errorTextView.isVisible = false
                }
                is UiState.Success -> {
                    val loginData = state.data
                    val account = loginData.account
                    Log.i("LoginActivity", "Đăng nhập thành công. Dữ liệu nhận được: $account")

                    sessionManager.saveTokens(loginData.accessToken, loginData.refreshToken)
                    loginProgressBar.isVisible = false
                    loginButton.isEnabled = true
                    
                    Toast.makeText(this, "Chào mừng, ${account.fullName}!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is UiState.Error -> {
                    loginProgressBar.isVisible = false
                    loginButton.isEnabled = true
                    errorTextView.text = "Tài khoản hoặc mật khẩu không đúng"
                    errorTextView.isVisible = true
                }
                else -> {
                    loginProgressBar.isVisible = false
                    loginButton.isEnabled = true
                    errorTextView.isVisible = false
                }
            }
        }
    }

    private fun signInWithMicrosoft() {
        if (mSingleAccountApp == null) {
            return
        }

        
        val scopes = arrayOf("User.Read")
        mSingleAccountApp?.signIn(this, null, scopes, getAuthInteractiveCallback())
    }

    private fun getAuthInteractiveCallback(): AuthenticationCallback {
        return object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                
                val account = authenticationResult.account
                Log.d("MSAL", "Đăng nhập thành công. Tên: ${account.username}")
                Toast.makeText(this@LoginActivity, "Đăng nhập thành công với: ${account.username}", Toast.LENGTH_LONG).show()
                navigateToMain()
            }

            override fun onError(exception: MsalException) {
                Log.e("MSAL", "Lỗi xác thực: $exception")
                Toast.makeText(this@LoginActivity, "Đăng nhập thất bại: ${exception.message}", Toast.LENGTH_LONG).show()
            }

            override fun onCancel() {
                Log.d("MSAL", "Người dùng đã hủy đăng nhập.")
            }
        }
    }

    private fun loadAccount() {
        mSingleAccountApp?.getCurrentAccountAsync(object : ISingleAccountPublicClientApplication.CurrentAccountCallback {
            override fun onAccountLoaded(activeAccount: IAccount?) {
                if (activeAccount != null) {
                    
                    Log.d("MSAL", "Đã tìm thấy tài khoản đăng nhập trước đó: ${activeAccount.username}")
                    navigateToMain()
                }
            }
            
            override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {
                // Xử lý khi tài khoản thay đổi (ví dụ: người dùng đăng xuất và đăng nhập bằng tài khoản khác)
            }

            override fun onError(exception: MsalException) {
                Log.e("MSAL", "Lỗi khi tải tài khoản: $exception")
            }
        })
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}