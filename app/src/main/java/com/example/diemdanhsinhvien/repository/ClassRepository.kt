package com.example.diemdanhsinhvien.repository

import com.example.diemdanhsinhvien.database.dao.CourseDao
import com.example.diemdanhsinhvien.database.dao.StudentDao
import com.example.diemdanhsinhvien.database.entities.Class
import com.example.diemdanhsinhvien.database.relations.ClassWithStudentCount
import kotlinx.coroutines.flow.Flow

class ClassRepository(private val courseDao: CourseDao, private val studentDao: StudentDao) {

    fun getClassById(classId: Int): Flow<Class?> {
        return courseDao.getClassById(classId)
    }

    suspend fun insertClass(classData: Class) {
        courseDao.insertClass(classData)
    }

    suspend fun deleteClass(classData: Class) {
        courseDao.deleteClass(classData)
    }

    suspend fun updateClass(classData: Class) {
        courseDao.updateClass(classData)
    }

    fun getClassesWithStudentCount(): Flow<List<ClassWithStudentCount>> {
        return studentDao.getClassesWithStudentCount()
    }
}