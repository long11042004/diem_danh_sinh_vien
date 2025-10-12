package com.example.diemdanhsinhvien.network.apiservice

import com.example.diemdanhsinhvien.data.model.Account
import com.example.diemdanhsinhvien.data.request.*
import com.example.diemdanhsinhvien.data.response.ApiLoginResponse
import retrofit2.Response
import retrofit2.http.*

interface AccountApiService {

    @POST("accounts/register")
    suspend fun registerTeacher(
        @Body request: RegisterRequest
    ): Response<Unit>

    @POST("accounts/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiLoginResponse>

    @GET("accounts/details")
    suspend fun getAccountDetails(): Response<Account>

    @PUT("accounts/{id}")
    suspend fun update(
        @Path("id") id: Int,
        @Body account: Account
    ): Response<Unit>

    @DELETE("accounts/{id}")
    suspend fun deleteAccount(
        @Path("id") id: Int
    ): Response<Unit>

    @POST("accounts/refreshToken")
    suspend fun refreshToken(
        @Header("Authorization") refreshToken: String
    ): Response<ApiLoginResponse>
}