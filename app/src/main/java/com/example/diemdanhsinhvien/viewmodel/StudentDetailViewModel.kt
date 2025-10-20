package com.example.diemdanhsinhvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.diemdanhsinhvien.common.UiState
import androidx.lifecycle.viewModelScope
import com.example.diemdanhsinhvien.data.relations.StudentAttendanceHistory
import com.example.diemdanhsinhvien.repository.StudentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StudentDetailViewModel(
    studentRepository: StudentRepository,
    studentId: Int
) : ViewModel() {

    val attendanceHistory: StateFlow<UiState<List<StudentAttendanceHistory>>> =
        studentRepository.getAttendanceHistory(studentId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
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