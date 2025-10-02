package com.example.diemdanhsinhvien.repository

import com.example.diemdanhsinhvien.database.dao.AttendanceDao
import com.example.diemdanhsinhvien.database.dao.CourseDao
import com.example.diemdanhsinhvien.database.dao.StudentDao
import com.example.diemdanhsinhvien.model.Report
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReportRepository(
    private val courseDao: CourseDao,
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao
) {
    fun getReports(): Flow<List<Report>> {
        return courseDao.getAllClasses().map { classList ->
            classList.map { classEntity ->
                val totalStudents = studentDao.getStudentCountForClass(classEntity.id)
                val totalSessions = attendanceDao.getUniqueSessionDatesForClass(classEntity.id).size
                val totalPresents = attendanceDao.getPresentCountForClass(classEntity.id)

                val totalPossibleAttendances = totalStudents * totalSessions
                val attendanceRate = if (totalPossibleAttendances > 0) {
                    (totalPresents * 100) / totalPossibleAttendances
                } else {
                    0 // Tránh chia cho 0 nếu chưa có buổi học hoặc sinh viên nào
                }

                Report(
                    courseName = classEntity.courseName,
                    classCode = classEntity.classCode,
                    attendanceRate = attendanceRate
                )
            }
        }
    }
}