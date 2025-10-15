package com.example.diemdanhsinhvien.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.model.Account
import com.example.diemdanhsinhvien.data.request.LoginRequest
import com.example.diemdanhsinhvien.data.request.RegisterRequest
import com.example.diemdanhsinhvien.data.request.UpdateAccountRequest
import com.example.diemdanhsinhvien.data.response.ApiLoginResponse
import com.example.diemdanhsinhvien.data.response.LoginResponse
import com.example.diemdanhsinhvien.repository.AccountRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val accountRepository: AccountRepository) : ViewModel() {

    // Chức năng Đăng nhập
    private val _loginResult = MutableLiveData<UiState<ApiLoginResponse>>()
    val loginResult: LiveData<UiState<ApiLoginResponse>> = _loginResult

    fun login(loginRequest: LoginRequest) {
        viewModelScope.launch {
            accountRepository.login(loginRequest).collect { state ->
                _loginResult.postValue(state)
            }
        }
    }

    // Chức năng Đăng ký
    private val _registerResult = MutableLiveData<UiState<Unit>>()
    val registerResult: LiveData<UiState<Unit>> = _registerResult

    fun register(registerRequest: RegisterRequest) {
        viewModelScope.launch {
            accountRepository.register(registerRequest).collect { state ->
                _registerResult.postValue(state)
            }
        }
    }

    // Chức năng lấy thông tin tài khoản chi tiết
    private val _accountDetails = MutableLiveData<UiState<Account>>()
    val accountDetails: LiveData<UiState<Account>> = _accountDetails

    fun getAccountDetails() {
        viewModelScope.launch {
            accountRepository.getAccountDetails().collect { state ->
                _accountDetails.postValue(state)
            }
        }
    }

    private val _updateState = MutableLiveData<UiState<Unit>>()
    val updateState: LiveData<UiState<Unit>> = _updateState

    fun updateAccount(id: Int, account: Account) {
        viewModelScope.launch {
            accountRepository.updateAccount(id, account).collect { state ->
                _updateState.postValue(state)
            }
        }
    }

}

class AuthViewModelFactory(private val repository: AccountRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}