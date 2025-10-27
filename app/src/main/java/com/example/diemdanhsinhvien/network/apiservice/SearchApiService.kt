package com.example.diemdanhsinhvien.network.apiservice

import com.example.diemdanhsinhvien.data.model.Account
import com.example.diemdanhsinhvien.data.model.Class
import com.example.diemdanhsinhvien.data.model.Student
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApiService {
    @GET("students/search")
    suspend fun searchStudents(@Query("q") query: String): List<Student>

    @GET("courses/search")
    suspend fun searchClasses(@Query("q") query: String): List<Class>

    @GET("accounts/search")
    suspend fun searchLecturers(@Query("q") query: String): List<Account>
}
