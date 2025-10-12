package com.example.diemdanhsinhvien.repository

import com.example.diemdanhsinhvien.adapter.AttendanceStatus
import com.example.diemdanhsinhvien.data.model.AttendanceRecord
import com.example.diemdanhsinhvien.data.model.AttendanceSession
import com.example.diemdanhsinhvien.network.apiservice.AttendanceApiService

class AttendanceRepository(private val attendanceApi: AttendanceApiService) {

    suspend fun saveAttendance(classId: Int, attendanceData: Map<Int, AttendanceStatus>) {
        val newSession = AttendanceSession(
            id=(1000..999999).random(),
            classId = classId,
            date = System.currentTimeMillis()
        )

        val sessionResponse = attendanceApi.insertAttendanceSession(newSession)
        if (!sessionResponse.isSuccessful || sessionResponse.body() == null) {
            throw Exception("Không thể tạo session điểm danh")
        }

        val sessionId = sessionResponse.body()!!.toInt()

        val records = attendanceData.map { (studentId, status) ->
            AttendanceRecord(
                id=(1000..999999).random(),
                sessionId = sessionId,
                studentId = studentId,
                status = status
            )
        }

        val recordResponse = attendanceApi.insertAttendanceRecords(records)
        if (!recordResponse.isSuccessful) {
            throw Exception("Không thể lưu bản ghi điểm danh")
        }
    }
}