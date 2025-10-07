package com.example.diemdanhsinhvien.database.entities

data class Class(
    val id: Int,
    val courseName: String,   // Tên học phần
    val courseId: String,     // Mã học phần
    val classCode: String,    // Mã lớp học
    val semester: String,     // Kì học
    val scheduleInfo: String  // Thời gian
)
