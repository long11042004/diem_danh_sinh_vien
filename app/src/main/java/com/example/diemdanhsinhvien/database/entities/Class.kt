package com.example.diemdanhsinhvien.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Class(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseName: String,   // Tên học phần
    val courseId: String,     // Mã học phần
    val classCode: String,    // Mã lớp học
    val semester: String,     // Kì học
    val scheduleInfo: String  // Thời gian
)
