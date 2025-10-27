package com.example.diemdanhsinhvien.network.apiservice

import android.content.Context
import com.example.diemdanhsinhvien.manager.SessionManager
import com.example.diemdanhsinhvien.network.auth.AuthInterceptor
import com.example.diemdanhsinhvien.network.auth.TokenAuthenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object APIClient {
    private const val BASE_URL = "http://10.0.2.2:2025/"

    private fun createOkHttpClient(context: Context): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val sessionManager = SessionManager(context.applicationContext)
        val authInterceptor = AuthInterceptor(sessionManager)
        val tokenAuthenticator = TokenAuthenticator(context.applicationContext, sessionManager)

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun createTokenRefreshHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private fun getRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun getTokenRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createTokenRefreshHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // API cho các request thông thường
    fun accountApi(context: Context): AccountApiService = getRetrofit(context).create(
        AccountApiService::class.java)
    fun courseApi(context: Context): CourseApiService = getRetrofit(context).create(CourseApiService::class.java)
    fun studentApi(context: Context): StudentApiService = getRetrofit(context).create(
        StudentApiService::class.java)
    fun attendanceApi(context: Context): AttendanceApiService = getRetrofit(context).create(
        AttendanceApiService::class.java)

    fun searchApi(context: Context): SearchApiService = getRetrofit(context).create(SearchApiService::class.java)

    // API chỉ dùng để refresh token
    fun tokenApi(context: Context): AccountApiService = getTokenRetrofit().create(AccountApiService::class.java)
}