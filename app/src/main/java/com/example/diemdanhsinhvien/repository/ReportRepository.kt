package com.example.diemdanhsinhvien.repository

import android.util.Log
import com.example.diemdanhsinhvien.data.model.Report
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.network.apiservice.AttendanceApiService
import com.example.diemdanhsinhvien.network.apiservice.CourseApiService
import com.example.diemdanhsinhvien.network.apiservice.StudentApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class ReportRepository(
    private val courseApi: CourseApiService,
    private val studentApi: StudentApiService,
    private val attendanceApi: AttendanceApiService
) {
    fun getReports(): Flow<UiState<List<Report>>> = flow {
    emit(UiState.Loading)

    try {
        val classesResp = courseApi.getAllClasses()
        if (!classesResp.isSuccessful) {
            emit(UiState.Error("Không thể tải danh sách lớp: ${classesResp.code()}"))
            return@flow
        }
        val classes = classesResp.body() ?: emptyList()

        val reports = coroutineScope {
            classes.map { classEntity ->
                async {
                    try {
                        val studentCountResp = studentApi.getStudentCountForClass(classEntity.id)
                        val totalStudents =
                            if (studentCountResp.isSuccessful)
                                studentCountResp.body() ?: 0
                            else 0

                        val datesResp = attendanceApi.getUniqueSessionDatesForClass(classEntity.id)
                        val uniqueDates =
                            if (datesResp.isSuccessful)
                                datesResp.body() ?: emptyList()
                            else emptyList()
                        val totalSessions = uniqueDates.size

                        val presentResp = attendanceApi.getPresentCountForClass(classEntity.id)
                        val totalPresents =
                            if (presentResp.isSuccessful)
                                presentResp.body() ?: 0
                            else 0

                        val totalPossibleAttendances = totalStudents * totalSessions
                        val attendanceRate = if (totalPossibleAttendances > 0) {
                            (totalPresents.toDouble() * 100.0) / totalPossibleAttendances
                        } else 0.0

                        val report = Report(
                            courseName = classEntity.courseName,
                            classCode = classEntity.classCode,
                            attendanceRate = attendanceRate
                        )
                        Log.d(
                            "ReportRepository",
                            "Báo cáo được tạo cho lớp '${report.courseName}' (${report.classCode}): " +
                                    "Tỷ lệ = ${report.attendanceRate}%. " +
                                    "Chi tiết: students=$totalStudents, sessions=$totalSessions, presents=$totalPresents"
                        )
                        report
                    } catch (e: Exception) {
                        Log.e("ReportRepository", "Lỗi khi xử lý lớp ${classEntity.courseName}: ${e.message}")
                        Report(
                            courseName = classEntity.courseName,
                            classCode = classEntity.classCode,
                            attendanceRate = 0.0
                        )
                    }
                }
            }.awaitAll()
        }

        emit(UiState.Success(reports))
    } catch (e: Exception) {
        e.printStackTrace()
        emit(UiState.Error("Lỗi kết nối hoặc xử lý: ${e.message}"))
    }
}
}