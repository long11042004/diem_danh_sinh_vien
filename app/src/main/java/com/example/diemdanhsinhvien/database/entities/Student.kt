package com.example.diemdanhsinhvien.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    foreignKeys = [ForeignKey(
        entity = Class::class,
        parentColumns = ["id"],
        childColumns = ["classId"],
        onDelete = ForeignKey.CASCADE // Tự động xóa sinh viên khi lớp học bị xóa
    )],
    indices = [Index(value = ["classId"])] // Tăng hiệu suất truy vấn theo classId
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val classId: Int,
    val studentName: String,
    val studentId: String
)

