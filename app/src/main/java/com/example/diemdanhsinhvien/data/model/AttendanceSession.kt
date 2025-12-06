package com.example.diemdanhsinhvien.data.model

data class AttendanceSession(
    val id: Int,
    val classId: Int,
    val date: Long,
    val startTime: Long,
    val endTime: Long,
    val sessionCode: String? = null // Thêm mã phiên
)