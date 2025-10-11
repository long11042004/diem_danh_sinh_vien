package com.example.diemdanhsinhvien.network

import com.example.diemdanhsinhvien.data.model.Student
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StudentApiService {
    @GET("students/class/{classId}/sort/name")
    suspend fun getStudentsByClassSortedByName(
        @Path("classId") classId: Int
    ): Response<List<Student>>

    @GET("students/class/{classId}/sort/code")
    suspend fun getStudentsByClassSortedByCode(
        @Path("classId") classId: Int
    ): Response<List<Student>>

    @POST("students")
    suspend fun insertStudent(
        @Body student: Student
    ): Response<Student>

    @DELETE("students/{id}")
    suspend fun deleteStudent(
        @Path("id") id: Int
    ): Response<Unit>

    @GET("students/count/{classId}")
    suspend fun getStudentCountForClass(
        @Path("classId") classId: Int
    ): Response<Int>
}