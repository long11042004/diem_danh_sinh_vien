package com.example.diemdanhsinhvien.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diemdanhsinhvien.database.entities.Student
import com.example.diemdanhsinhvien.database.relations.ClassWithStudentCount
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY studentName ASC")
    fun getStudentsByClassSortedByName(classId: Int): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY studentId ASC")
    fun getStudentsByClassSortedByCode(classId: Int): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("""
        SELECT courses.*, COUNT(students.id) as studentCount
        FROM courses
        LEFT JOIN students ON courses.id = students.classId
        GROUP BY courses.id
        ORDER BY courses.courseName ASC
    """)
    fun getClassesWithStudentCount(): Flow<List<ClassWithStudentCount>>

    @Query("SELECT COUNT(id) FROM students WHERE classId = :classId")
    suspend fun getStudentCountForClass(classId: Int): Int
}