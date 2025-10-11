package com.example.diemdanhsinhvien.data.model

data class Class(
    val id: Int,
    val courseName: String,   // Tên học phần
    val classCode: String,     // Mã lớp
    val courseId: String,    // Mã học phần
    val semester: String,     // Kì học
    val scheduleInfo: String  // Lịch học
)
