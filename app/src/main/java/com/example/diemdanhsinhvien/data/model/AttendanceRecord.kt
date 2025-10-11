package com.example.diemdanhsinhvien.data.model

import com.example.diemdanhsinhvien.adapter.AttendanceStatus

data class AttendanceRecord(
    val id: Int,
    val sessionId: Int,
    val studentId: Int,
    val status: AttendanceStatus
)