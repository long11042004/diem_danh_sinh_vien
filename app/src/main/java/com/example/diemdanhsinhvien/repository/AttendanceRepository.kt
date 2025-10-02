package com.example.diemdanhsinhvien.repository

import com.example.diemdanhsinhvien.adapter.AttendanceStatus
import com.example.diemdanhsinhvien.database.dao.AttendanceDao
import com.example.diemdanhsinhvien.database.entities.AttendanceRecord
import com.example.diemdanhsinhvien.database.entities.AttendanceSession


class AttendanceRepository(private val attendanceDao: AttendanceDao) {

    suspend fun saveAttendance(classId: Int, attendanceData: Map<Int, AttendanceStatus>) {
        val newSession = AttendanceSession(classId = classId, date = System.currentTimeMillis())
        val sessionId = attendanceDao.insertAttendanceSession(newSession)

        val records = attendanceData.map { (studentId, status) ->
            AttendanceRecord(sessionId = sessionId.toInt(), studentId = studentId, status = status)
        }
        attendanceDao.insertAttendanceRecords(records)
    }
}