package com.example.diemdanhsinhvien.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_sessions",
    foreignKeys = [ForeignKey(
        entity = Class::class,
        parentColumns = ["id"],
        childColumns = ["classId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["classId"])]
)
data class AttendanceSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val classId: Int,
    val date: Long
)