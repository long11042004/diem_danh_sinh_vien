package com.example.diemdanhsinhvien.network.apiservice

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import com.example.diemdanhsinhvien.data.model.AttendanceRecord
import com.example.diemdanhsinhvien.data.response.ActiveSessionResponse
import com.example.diemdanhsinhvien.data.model.AttendanceSession
import com.example.diemdanhsinhvien.data.model.ClassReportDetail
import com.example.diemdanhsinhvien.data.relations.StudentAttendanceHistory

interface AttendanceApiService {

    @POST("sessions")
    suspend fun insertAttendanceSession(
        @Body session: AttendanceSession
    ): Response<AttendanceSession> // Thay đổi: Trả về toàn bộ object session mới được tạo

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
    ): Response<List<String>>

    @GET("attendance/class/{classId}/presentCount")
    suspend fun getPresentCountForClass(
        @Path("classId") classId: Int
    ): Response<Int>

    @GET("attendance/report/class/{classId}")
    suspend fun getReportDetailsForClass(
        @Path("classId") classId: Int
    ): Response<List<ClassReportDetail>>    
	
	@GET("sessions/active/{classId}")
    suspend fun getActiveSessionForClass(
        @Path("classId") classId: Int
    ): Response<ActiveSessionResponse>
}