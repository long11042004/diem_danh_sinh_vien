package com.example.diemdanhsinhvien.database.entities

import com.example.diemdanhsinhvien.adapter.AttendanceStatus

data class AttendanceRecord(
    val id: Int,
    val sessionId: Int,
    val studentId: Int,
    val status: AttendanceStatus
)