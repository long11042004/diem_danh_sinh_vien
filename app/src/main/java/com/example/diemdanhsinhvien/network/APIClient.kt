package com.example.diemdanhsinhvien.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object APIClient {
    private const val BASE_URL = "http://192.168.0.213:2025/api/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
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
}