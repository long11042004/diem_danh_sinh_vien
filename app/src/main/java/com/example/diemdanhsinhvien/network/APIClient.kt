package com.example.diemdanhsinhvien.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object APIClient {
    private const val BASE_URL = "http://10.0.2.2:2025/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val studentApi: StudentApiService by lazy {
        retrofit.create(StudentApiService::class.java)
    }

    val courseApi: CourseApiService by lazy {
        retrofit.create(CourseApiService::class.java)
    }

    val attendanceApi: AttendanceApiService by lazy {
        retrofit.create(AttendanceApiService::class.java)
    }

    val accountApi: AccountApiService by lazy {
        retrofit.create(AccountApiService::class.java)
    }
}