package com.example.diemdanhsinhvien.network.apiservice

import com.example.diemdanhsinhvien.data.relations.ClassWithStudentCount
import com.example.diemdanhsinhvien.data.model.Class
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CourseApiService {
    @GET("courses")
    suspend fun getAllClasses(): Response<List<Class>>

    @GET("courses/withStudentCount")
    suspend fun getClassesWithStudentCount(): Response<List<ClassWithStudentCount>>

    @GET("courses/withStudentCount/teacher/{teacherId}")
    suspend fun getClassesByTeacher(
        @Path("teacherId") teacherId: Int
    ): Response<List<ClassWithStudentCount>>

    @GET("courses/{id}")
    suspend fun getClassById(
        @Path("id") classId: Int
    ): Response<Class?>

    @POST("courses")
    suspend fun insertClass(
        @Body classData: Class
    ): Response<Class>

    @PUT("courses/{id}")
    suspend fun updateClass(
        @Path("id") classId: Int,
        @Body classData: Class
    ): Response<Class>

    @DELETE("courses/{id}")
    suspend fun deleteClass(
        @Path("id") classId: Int
    ): Response<Unit>
}