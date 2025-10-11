package com.example.diemdanhsinhvien.data.relations

data class ClassWithStudentCount(
    val id: Int,
    val courseName: String,
    val classCode: String,
    val courseId: String,
    val semester: String,
    val scheduleInfo: String?,
    val studentCount: Int
)