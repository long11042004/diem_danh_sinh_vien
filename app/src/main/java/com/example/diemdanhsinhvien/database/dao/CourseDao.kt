package com.example.diemdanhsinhvien.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.diemdanhsinhvien.database.entities.Class
import com.example.diemdanhsinhvien.database.relations.ClassWithStudentCount
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY courseName ASC")
    fun getAllClasses(): Flow<List<Class>>

    @Query("SELECT * FROM courses WHERE id = :classId")
    fun getClassById(classId: Int): Flow<Class?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertClass(classData: Class)

    @Delete
    suspend fun deleteClass(classData: Class)
    
    @Update
    suspend fun updateClass(classData: Class)
}