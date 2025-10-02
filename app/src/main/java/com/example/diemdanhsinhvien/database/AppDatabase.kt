package com.example.diemdanhsinhvien.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.diemdanhsinhvien.database.dao.*
import com.example.diemdanhsinhvien.database.entities.AttendanceSession
import com.example.diemdanhsinhvien.database.entities.AttendanceRecord
import com.example.diemdanhsinhvien.database.entities.Class
import com.example.diemdanhsinhvien.database.entities.Student

@Database(
    entities = [
        Class::class, 
        Student::class, 
        AttendanceSession::class,
        AttendanceRecord::class
    ],
    version = 2,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "school_database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
