package com.example.diemdanhsinhvien.database

import androidx.room.TypeConverter
import com.example.diemdanhsinhvien.adapter.AttendanceStatus

class Converters {
    @TypeConverter
    fun fromAttendanceStatus(status: AttendanceStatus): String {
        return status.name
    }

    @TypeConverter
    fun toAttendanceStatus(status: String): AttendanceStatus {
        return AttendanceStatus.valueOf(status)
    }
}