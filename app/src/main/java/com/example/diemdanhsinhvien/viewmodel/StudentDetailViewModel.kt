package com.example.diemdanhsinhvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diemdanhsinhvien.database.relations.StudentAttendanceHistory
import com.example.diemdanhsinhvien.repository.StudentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StudentDetailViewModel(
    studentRepository: StudentRepository,
    studentId: Int
) : ViewModel() {

    val attendanceHistory: StateFlow<List<StudentAttendanceHistory>> =
        studentRepository.getAttendanceHistory(studentId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}

class StudentDetailViewModelFactory(
    private val studentRepository: StudentRepository,
    private val studentId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentDetailViewModel(studentRepository, studentId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}