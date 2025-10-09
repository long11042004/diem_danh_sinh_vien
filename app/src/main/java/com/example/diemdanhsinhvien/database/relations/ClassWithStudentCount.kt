package com.example.diemdanhsinhvien.database.relations

import com.example.diemdanhsinhvien.database.entities.Class

data class ClassWithStudentCount(
    val id: Int,
    val courseName: String,
    val classCode: String,
    val courseId: String,
    val semester: String,
    val scheduleInfo: String?,
    val studentCount: Int
)