package com.example.diemdanhsinhvien.repository

import android.util.Log
import com.example.diemdanhsinhvien.data.model.Class
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.relations.ClassWithStudentCount
import com.example.diemdanhsinhvien.network.apiservice.CourseApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ClassRepository(private val courseApi: CourseApiService) {

    fun getClassById(classId: Int): Flow<Class?> = flow {
        val response = courseApi.getClassById(classId)
        if (response.isSuccessful) {
            emit(response.body())
        } else {
            emit(null)
        }
    }

    suspend fun insertClass(classData: Class): Class? {
        val response = courseApi.insertClass(classData)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun deleteClass(classData: Class): Boolean {
        val response = courseApi.deleteClass(classData.id)
        return response.isSuccessful
    }

    suspend fun updateClass(classData: Class): Class? {
        val response = courseApi.updateClass(classData.id!!, classData)
        return if (response.isSuccessful) response.body() else null
    }

    fun getClassesWithStudentCount(teacherId: Int): Flow<UiState<List<ClassWithStudentCount>>> = flow {
        emit(UiState.Loading)
        try {
            val response = courseApi.getClassesByTeacher(teacherId)
            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                Log.d("API", "Response: $list")
                emit(UiState.Success(list))
            } else {
                emit(UiState.Error("Lỗi API: ${response.code()}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(UiState.Error("Lỗi kết nối: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
}