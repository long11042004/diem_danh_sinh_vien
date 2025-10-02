package com.example.diemdanhsinhvien.database.relations

import androidx.room.Embedded
import com.example.diemdanhsinhvien.database.entities.Class

data class ClassWithStudentCount(
    @Embedded val classInfo: Class,
    val studentCount: Int
)