package com.example.diemdanhsinhvien.repository

import com.example.diemdanhsinhvien.adapter.AttendanceStatus
import com.example.diemdanhsinhvien.data.model.AttendanceRecord
import com.example.diemdanhsinhvien.data.model.AttendanceSession
import com.example.diemdanhsinhvien.network.apiservice.AttendanceApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AttendanceRepository(private val attendanceApi: AttendanceApiService) {

    suspend fun saveAttendance(classId: Int, attendanceData: Map<Int, AttendanceStatus>) {
        val currentTime = System.currentTimeMillis()
        val newSession = AttendanceSession(
            id = 0, // Server sẽ tự gán id
            classId = classId,
            date = currentTime,
            startTime = currentTime,
            endTime = currentTime,
            sessionCode = null
        )

        val sessionResponse = attendanceApi.insertAttendanceSession(newSession)
        if (!sessionResponse.isSuccessful || sessionResponse.body() == null) {
            throw Exception("Không thể tạo session điểm danh")
        }

        val sessionId = sessionResponse.body()!!.id

        val records = attendanceData.map { (studentId, status) ->
            AttendanceRecord(
                id = 0, // Server sẽ tự gán id
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

    fun getActiveSession(classId: Int): Flow<AttendanceSession?> = flow {
        try {
            val response = attendanceApi.getActiveSessionForClass(classId)
            if (response.isSuccessful) {
                val activeSessionResponse = response.body()
                if (activeSessionResponse != null && activeSessionResponse.isActive) {
                    // Phản hồi thành công và có phiên hoạt động.
                    // Tạo một đối tượng AttendanceSession từ dữ liệu nhận được.
                    val session = AttendanceSession(
                        id = activeSessionResponse.sessionId ?: 0,
                        classId = classId, // Lấy từ tham số của hàm
                        date = 0L, // Không có trong phản hồi, đặt giá trị mặc định
                        startTime = 0L, // Không có trong phản hồi, đặt giá trị mặc định
                        endTime = activeSessionResponse.endTime ?: 0L,
                        sessionCode = activeSessionResponse.sessionCode
                    )
                    emit(session)
                } else {
                    // Phản hồi thành công nhưng không có phiên hoạt động (isActive = false) hoặc body là null
                    emit(null)
                }
            } else {
                // API trả về 404 Not Found nếu không có session active, đây là trường hợp bình thường
                if (response.code() == 404) {
                    emit(null)
                } else {
                    throw Exception("Lỗi API: ${response.code()} - ${response.message()}")
                }
            }
        } catch (e: Exception) {
            throw Exception("Lỗi khi lấy phiên hoạt động: ${e.message}", e)
        }
    }.flowOn(Dispatchers.IO)

    fun createNewSession(classId: Int, durationMinutes: Long): Flow<AttendanceSession?> = flow {
        try {
            val currentTime = System.currentTimeMillis()
            val endTime = currentTime + durationMinutes * 60 * 1000
            val newSessionRequest = AttendanceSession(
                id = 0, // Server sẽ tự gán id
                classId = classId,
                date = currentTime,
                startTime = currentTime,
                endTime = endTime
            )

            val response = attendanceApi.insertAttendanceSession(newSessionRequest)

            if (response.isSuccessful) {
                emit(response.body())
            } else {
                throw Exception("Không thể tạo phiên điểm danh mới: ${response.code()}")
            }
        } catch (e: Exception) {
            throw Exception("Lỗi khi tạo phiên điểm danh: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)
}