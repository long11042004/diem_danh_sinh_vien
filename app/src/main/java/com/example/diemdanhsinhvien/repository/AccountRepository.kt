package com.example.diemdanhsinhvien.repository

import android.util.Log
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.model.Account
import com.example.diemdanhsinhvien.data.request.ChangePasswordRequest
import com.example.diemdanhsinhvien.data.request.LoginRequest
import com.example.diemdanhsinhvien.data.request.RegisterRequest
import com.example.diemdanhsinhvien.data.response.ApiLoginResponse
import com.example.diemdanhsinhvien.data.response.ErrorResponse
import com.example.diemdanhsinhvien.network.apiservice.AccountApiService
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AccountRepository(private val accountApi: AccountApiService) {

    fun login(loginRequest: LoginRequest): Flow<UiState<ApiLoginResponse>> = flow {
        emit(UiState.Loading)
        try {
            val response = accountApi.login(loginRequest)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                emit(UiState.Success(apiResponse))
            } else {
                emit(UiState.Error("Đăng nhập thất bại: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(UiState.Error("Lỗi kết nối: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    fun register(registerRequest: RegisterRequest): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)
        try {
            val response = accountApi.register(registerRequest)
            if (response.isSuccessful) {
                emit(UiState.Success(Unit))
            } else {
                emit(UiState.Error("Đăng ký thất bại: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(UiState.Error("Lỗi kết nối: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    fun getAccountDetails(): Flow<UiState<Account>> = flow {
        emit(UiState.Loading)
        try {
            val response = accountApi.getAccountDetails()

            if (response.isSuccessful) {
                response.body()?.let { accountDetails ->
                    emit(UiState.Success(accountDetails))
                    Log.i("AccountRepository", "Chi tiết tài khoản: $accountDetails")
                } ?: emit(UiState.Error("Không nhận được dữ liệu tài khoản."))
            } else {
                emit(UiState.Error("Lỗi ${response.code()}: ${response.message()}"))
                Log.i("AccountRepository", "Lỗi ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            emit(UiState.Error(e.localizedMessage ?: "Đã có lỗi không xác định xảy ra"))
        }
    }.flowOn(Dispatchers.IO)

    fun getAccountById(id: Int): Flow<UiState<Account>> = flow {
        emit(UiState.Loading)
        try {
            val response = accountApi.getAccountById(id)

            if (response.isSuccessful) {
                response.body()?.let { accountDetails ->
                    emit(UiState.Success(accountDetails))
                    Log.i("AccountRepository", "Chi tiết tài khoản theo ID: $accountDetails")
                } ?: emit(UiState.Error("Không nhận được dữ liệu tài khoản."))
            } else {
                emit(UiState.Error("Lỗi ${response.code()}: ${response.message()}"))
                Log.i("AccountRepository", "Lỗi ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            emit(UiState.Error(e.localizedMessage ?: "Đã có lỗi không xác định xảy ra"))
        }
    }.flowOn(Dispatchers.IO)

    fun updateAccount(id: Int, account: Account): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)
        try {
            val response = accountApi.update(id, account)
            if (response.isSuccessful) {
                emit(UiState.Success(Unit))
            } else {
                val errorBody = response.errorBody()?.string()
                var errorMessage = "Lỗi ${response.code()}: ${response.message()}" // Default message

                if (!errorBody.isNullOrEmpty()) {
                    try {
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                        val apiMessage = errorResponse.message ?: errorResponse.error
                        if (!apiMessage.isNullOrBlank()) {
                            errorMessage = apiMessage
                        }
                    } catch (e: JsonSyntaxException) {
                        // The error body is not a valid JSON. It's probably a plain text message.
                        errorMessage = errorBody
                    }
                }
                emit(UiState.Error(errorMessage))
                Log.e("AccountRepository", "Lỗi cập nhật tài khoản: $errorMessage")
            }
        } catch (e: Exception) {
            Log.e("AccountRepository", "Ngoại lệ khi cập nhật tài khoản", e)
            emit(UiState.Error(e.localizedMessage ?: "Đã có lỗi không xác định xảy ra"))
        }
    }.flowOn(Dispatchers.IO)

    fun changePassword(request: ChangePasswordRequest): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)
        try {
            val response = accountApi.changePassword(request)
            if (response.isSuccessful) {
                emit(UiState.Success(Unit))
            } else {
                val errorBody = response.errorBody()?.string()
                var errorMessage = "Lỗi ${response.code()}: ${response.message()}" // Default message

                if (!errorBody.isNullOrEmpty()) {
                    try {
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                        val apiMessage = errorResponse.message ?: errorResponse.error
                        if (!apiMessage.isNullOrBlank()) {
                            errorMessage = apiMessage
                        }
                    } catch (e: JsonSyntaxException) {
                        // The error body is not a valid JSON. It's probably a plain text message.
                        errorMessage = errorBody
                    }
                }
                emit(UiState.Error(errorMessage))
                Log.e("AccountRepository", "Lỗi đổi mật khẩu: $errorMessage")
            }
        } catch (e: Exception) {
            Log.e("AccountRepository", "Ngoại lệ khi đổi mật khẩu", e)
            emit(UiState.Error(e.localizedMessage ?: "Đã có lỗi không xác định xảy ra"))
        }
    }.flowOn(Dispatchers.IO)
}