package com.example.diemdanhsinhvien.repository

import com.example.diemdanhsinhvien.data.model.Account
import com.example.diemdanhsinhvien.data.model.Class
import com.example.diemdanhsinhvien.network.apiservice.SearchApiService
import com.example.diemdanhsinhvien.data.model.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepository(private val searchApiService: SearchApiService) {

    suspend fun searchStudents(query: String): Result<List<Student>> {
        return withContext(Dispatchers.IO) {
            try {
                val students = searchApiService.searchStudents(query)
                Result.success(students)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    suspend fun searchClasses(query: String): Result<List<Class>> {
        return withContext(Dispatchers.IO) {
            try {
                val classes = searchApiService.searchClasses(query)
                Result.success(classes)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    suspend fun searchLecturers(query: String): Result<List<Account>> {
        return withContext(Dispatchers.IO) {
            try {
                val lecturers = searchApiService.searchLecturers(query)
                Result.success(lecturers)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
}
