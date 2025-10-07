package com.example.diemdanhsinhvien.repository

import com.example.diemdanhsinhvien.database.entities.Class
import com.example.diemdanhsinhvien.database.relations.ClassWithStudentCount
import com.example.diemdanhsinhvien.network.APIClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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

    fun getClassesWithStudentCount(): Flow<List<ClassWithStudentCount>> = flow {
        try {
            val response = APIClient.studentApi.getClassesWithStudentCount()
            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                emit(list)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }
}