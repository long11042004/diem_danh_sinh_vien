package com.example.diemdanhsinhvien.repository

import com.example.diemdanhsinhvien.database.dao.AttendanceDao
import com.example.diemdanhsinhvien.database.dao.StudentDao
import com.example.diemdanhsinhvien.database.entities.Student
import com.example.diemdanhsinhvien.database.relations.StudentAttendanceHistory
import com.example.diemdanhsinhvien.viewmodel.SortOrder
import kotlinx.coroutines.flow.Flow

class StudentRepository(private val studentDao: StudentDao, private val attendanceDao: AttendanceDao) {

    fun getStudentsForClass(classId: Int, sortOrder: SortOrder): Flow<List<Student>> {
        return when (sortOrder) {
            SortOrder.BY_NAME -> studentDao.getStudentsByClassSortedByName(classId)
            SortOrder.BY_ID -> studentDao.getStudentsByClassSortedByCode(classId)
        }
    }

    fun getAttendanceHistory(studentId: Int): Flow<List<StudentAttendanceHistory>> {
        return attendanceDao.getAttendanceHistoryForStudent(studentId)
    }

    suspend fun insertStudent(student: Student) {
        studentDao.insertStudent(student)
    }

    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student)
    }

}