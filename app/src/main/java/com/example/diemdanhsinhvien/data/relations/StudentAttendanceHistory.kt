package com.example.diemdanhsinhvien.data.relations

import com.example.diemdanhsinhvien.adapter.AttendanceStatus

data class StudentAttendanceHistory(
    val date: Long,
    val status: AttendanceStatus
)