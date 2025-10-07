package com.example.diemdanhsinhvien.repository

import com.example.diemdanhsinhvien.model.Report
import com.example.diemdanhsinhvien.network.AttendanceApiService
import com.example.diemdanhsinhvien.network.CourseApiService
import com.example.diemdanhsinhvien.network.StudentApiService
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
    fun getReports(): Flow<List<Report>> = flow {
        try {
            val classesResp = courseApi.getAllClasses()
            val classes = if (classesResp.isSuccessful) classesResp.body() ?: emptyList() else emptyList()

            val reports = coroutineScope {
                classes.map { classEntity ->
                    async {
                        val studentCountResp = studentApi.getStudentCountForClass(classEntity.id)
                        val totalStudents = if (studentCountResp.isSuccessful) studentCountResp.body() ?: 0 else 0

                        val datesResp = attendanceApi.getUniqueSessionDatesForClass(classEntity.id)
                        val uniqueDates = if (datesResp.isSuccessful) datesResp.body() ?: emptyList() else emptyList()
                        val totalSessions = uniqueDates.size

                        val presentResp = attendanceApi.getPresentCountForClass(classEntity.id)
                        val totalPresents = if (presentResp.isSuccessful) presentResp.body() ?: 0 else 0

                        val totalPossibleAttendances = totalStudents * totalSessions
                        val attendanceRate = if (totalPossibleAttendances > 0) {
                            (totalPresents * 100) / totalPossibleAttendances
                        } else {
                            0
                        }
                        Report(
                            courseName = classEntity.courseName,
                            classCode = classEntity.classCode,
                            attendanceRate = attendanceRate
                        )
                    }
                }.awaitAll()
            }
            emit(reports)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}