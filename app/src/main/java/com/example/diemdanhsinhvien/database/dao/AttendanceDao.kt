package com.example.diemdanhsinhvien.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diemdanhsinhvien.database.entities.AttendanceRecord
import com.example.diemdanhsinhvien.database.entities.AttendanceSession
import com.example.diemdanhsinhvien.database.relations.StudentAttendanceHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceSession(session: AttendanceSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecords(records: List<AttendanceRecord>)

    @Query("""
        SELECT s.date, r.status
        FROM attendance_records r
        INNER JOIN attendance_sessions s ON r.sessionId = s.id
        WHERE r.studentId = :studentId
        ORDER BY s.date DESC
    """)
    fun getAttendanceHistoryForStudent(studentId: Int): Flow<List<StudentAttendanceHistory>>

    @Query("SELECT DISTINCT date FROM attendance_sessions WHERE classId = :classId")
    suspend fun getUniqueSessionDatesForClass(classId: Int): List<Long>

    @Query("""
        SELECT COUNT(ar.id)
        FROM attendance_records ar
        JOIN attendance_sessions asess ON ar.sessionId = asess.id
        WHERE asess.classId = :classId AND (ar.status = 'PRESENT' OR ar.status = 'LATE')
    """)
    suspend fun getPresentCountForClass(classId: Int): Int
}