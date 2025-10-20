package com.example.diemdanhsinhvien.repository

import com.example.diemdanhsinhvien.data.model.Student
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.relations.StudentAttendanceHistory
import com.example.diemdanhsinhvien.network.apiservice.AttendanceApiService
import com.example.diemdanhsinhvien.network.apiservice.StudentApiService
import com.example.diemdanhsinhvien.viewmodel.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class StudentRepository(
    private val studentApi: StudentApiService,
    private val attendanceApi: AttendanceApiService
) {
    fun getStudentsForClass(classId: Int, sortOrder: SortOrder): Flow<List<Student>> = flow {
        try {
            val response = when (sortOrder) {
                SortOrder.BY_NAME -> studentApi.getStudentsByClassSortedByName(classId)
                SortOrder.BY_ID -> studentApi.getStudentsByClassSortedByCode(classId)
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

    fun getAttendanceHistory(studentId: Int): Flow<UiState<List<StudentAttendanceHistory>>> = flow {
        emit(UiState.Loading)
        try {
            val response = attendanceApi.getAttendanceHistoryForStudent(studentId)
            if (response.isSuccessful) {
                val historyList = response.body() ?: emptyList()
                emit(UiState.Success(historyList))
            } else {
                emit(UiState.Error("Lỗi ${response.code()}: Không thể tải lịch sử điểm danh"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(UiState.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }

    suspend fun insertStudent(student: Student): Student? {
        val response = studentApi.insertStudent(student)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun deleteStudent(student: Student): Boolean {
        val response = studentApi.deleteStudent(student.id)
        return response.isSuccessful
    }
}