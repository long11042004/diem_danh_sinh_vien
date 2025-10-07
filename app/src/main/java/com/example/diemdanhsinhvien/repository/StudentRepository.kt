package com.example.diemdanhsinhvien.repository

import com.example.diemdanhsinhvien.database.entities.Student
import com.example.diemdanhsinhvien.database.relations.StudentAttendanceHistory
import com.example.diemdanhsinhvien.network.APIClient
import com.example.diemdanhsinhvien.viewmodel.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class StudentRepository {

    fun getStudentsForClass(classId: Int, sortOrder: SortOrder): Flow<List<Student>> = flow {
        try {
            val response = when (sortOrder) {
                SortOrder.BY_NAME -> APIClient.studentApi.getStudentsByClassSortedByName(classId)
                SortOrder.BY_ID -> APIClient.studentApi.getStudentsByClassSortedByCode(classId)
            }

            if (response.isSuccessful) {
                val students = response.body() ?: emptyList()
                emit(students)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    fun getAttendanceHistory(studentId: Int): Flow<List<StudentAttendanceHistory>> = flow {
        try {
            val response = APIClient.attendanceApi.getAttendanceHistoryForStudent(studentId)
            if (response.isSuccessful) {
                val historyList = response.body() ?: emptyList()
                emit(historyList)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    suspend fun insertStudent(student: Student): Student? {
        val response = APIClient.studentApi.insertStudent(student)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun deleteStudent(student: Student): Boolean {
        val response = APIClient.studentApi.deleteStudent(student.id)
        return response.isSuccessful
    }
}