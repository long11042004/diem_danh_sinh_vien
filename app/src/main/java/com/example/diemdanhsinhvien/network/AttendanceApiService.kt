package com.example.diemdanhsinhvien.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import com.example.diemdanhsinhvien.data.model.AttendanceRecord
import com.example.diemdanhsinhvien.data.model.AttendanceSession
import com.example.diemdanhsinhvien.data.relations.StudentAttendanceHistory

interface AttendanceApiService {

    @POST("sessions")
    suspend fun insertAttendanceSession(
        @Body session: AttendanceSession
    ): Response<Long>

    @POST("attendance/records")
    suspend fun insertAttendanceRecords(
        @Body records: List<AttendanceRecord>
    ): Response<Unit>

    @GET("attendance/history/{studentId}")
    suspend fun getAttendanceHistoryForStudent(
        @Path("studentId") studentId: Int
    ): Response<List<StudentAttendanceHistory>>

    @GET("sessions/class/{classId}/dates")
    suspend fun getUniqueSessionDatesForClass(
        @Path("classId") classId: Int
    ): Response<List<Long>>

    @GET("attendance/class/{classId}/presentCount")
    suspend fun getPresentCountForClass(
        @Path("classId") classId: Int
    ): Response<Int>
}