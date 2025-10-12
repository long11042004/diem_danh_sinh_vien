package com.example.diemdanhsinhvien.repository

import android.util.Log
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.model.Account
import com.example.diemdanhsinhvien.data.request.LoginRequest
import com.example.diemdanhsinhvien.data.request.RegisterRequest
import com.example.diemdanhsinhvien.data.response.ApiLoginResponse
import com.example.diemdanhsinhvien.network.apiservice.AccountApiService
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
            val response = accountApi.registerTeacher(registerRequest)
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
}