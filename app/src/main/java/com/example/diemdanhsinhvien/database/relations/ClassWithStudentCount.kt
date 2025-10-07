package com.example.diemdanhsinhvien.database.relations

import com.example.diemdanhsinhvien.database.entities.Class

data class ClassWithStudentCount(
    val classInfo: Class,
    val studentCount: Int
)