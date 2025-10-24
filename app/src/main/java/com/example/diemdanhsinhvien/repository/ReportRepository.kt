package com.example.diemdanhsinhvien.repository

import android.util.Log
import com.example.diemdanhsinhvien.data.model.ClassReportDetail
import com.example.diemdanhsinhvien.data.model.Report
import com.example.diemdanhsinhvien.network.apiservice.AttendanceApiService
import com.example.diemdanhsinhvien.network.apiservice.CourseApiService
import com.example.diemdanhsinhvien.network.apiservice.StudentApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import com.example.diemdanhsinhvien.common.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.coroutineScope

class ReportRepository(
    private val courseApi: CourseApiService,
    private val studentApi: StudentApiService,
    private val attendanceApi: AttendanceApiService
) {
    suspend fun getReportsForTeacher(teacherId: Int): List<Report> {
        val classesResp = courseApi.getClassesByTeacher(teacherId)
        if (!classesResp.isSuccessful) {
            throw Exception("Không thể tải danh sách lớp: ${classesResp.code()} - ${classesResp.message()}")
        }
        val classesWithCount = classesResp.body() ?: emptyList()

        return coroutineScope {
            classesWithCount.map { classWithCount ->
                async {
                    try {
                        val totalStudents = classWithCount.studentCount

                        val datesResp = attendanceApi.getUniqueSessionDatesForClass(classWithCount.id)
                        val uniqueDates =
                            if (datesResp.isSuccessful)
                                datesResp.body() ?: emptyList()
                            else emptyList()
                        val totalSessions = uniqueDates.size

                        val presentResp = attendanceApi.getPresentCountForClass(classWithCount.id)
                        val totalPresents =
                            if (presentResp.isSuccessful)
                                presentResp.body() ?: 0
                            else 0

                        val totalPossibleAttendances = totalStudents * totalSessions
                        val attendanceRate =
                            if (totalPossibleAttendances > 0) {
                                (totalPresents.toDouble() * 100.0) / totalPossibleAttendances
                            } else 0.0

                        Report(
                            classId = classWithCount.id,
                            courseName = classWithCount.courseName ?: "N/A",
                            classCode = classWithCount.classCode ?: "N/A",
                            attendanceRate = attendanceRate
                        )
                    } catch (e: Exception) {
                        Log.e("ReportRepository", "Lỗi khi xử lý lớp ${classWithCount.courseName}: ${e.message}")
                        Report(
                            classId = classWithCount.id,
                            courseName = classWithCount.courseName ?: "N/A",
                            classCode = classWithCount.classCode ?: "N/A",
                            attendanceRate = 0.0
                        )
                    }
                }
            }.awaitAll()
        }
    }

    fun getReportDetailsForClass(classId: Int): Flow<UiState<List<ClassReportDetail>>> = flow {
        emit(UiState.Loading)
        try {
            val response = attendanceApi.getReportDetailsForClass(classId)
            if (response.isSuccessful) {
                val details = response.body() ?: emptyList()

                val sortedDetails = details.sortedByDescending { it.sessionDate }

                emit(UiState.Success(sortedDetails))
            } else {
                emit(UiState.Error("Lỗi ${response.code()}: Không thể tải chi tiết báo cáo"))
            }
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
}