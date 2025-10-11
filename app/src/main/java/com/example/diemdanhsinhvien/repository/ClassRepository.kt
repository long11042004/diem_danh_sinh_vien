package com.example.diemdanhsinhvien.repository

import android.util.Log
import com.example.diemdanhsinhvien.data.model.Class
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.relations.ClassWithStudentCount
import com.example.diemdanhsinhvien.network.APIClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ClassRepository {

    fun getClassById(classId: Int): Flow<Class?> = flow {
        val response = APIClient.courseApi.getClassById(classId)
        if (response.isSuccessful) {
            emit(response.body())
        } else {
            emit(null)
        }
    }

    suspend fun insertClass(classData: Class): Class? {
        val response = APIClient.courseApi.insertClass(classData)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun deleteClass(classData: Class): Boolean {
        val response = APIClient.courseApi.deleteClass(classData.id)
        return response.isSuccessful
    }

    suspend fun updateClass(classData: Class): Class? {
        val response = APIClient.courseApi.updateClass(classData.id!!, classData)
        return if (response.isSuccessful) response.body() else null
    }

    fun getClassesWithStudentCount(): Flow<UiState<List<ClassWithStudentCount>>> = flow {
        emit(UiState.Loading)
        try {
            val response = APIClient.courseApi.getClassesWithStudentCount()
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