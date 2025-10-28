package com.example.diemdanhsinhvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.model.Student
import androidx.lifecycle.viewModelScope
import com.example.diemdanhsinhvien.common.HistoryUiState
import com.example.diemdanhsinhvien.repository.StudentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StudentDetailViewModel(
    studentRepository: StudentRepository,
    studentId: Int
) : ViewModel() {

    val studentDetail: StateFlow<UiState<Student>> =
        studentRepository.getStudentById(studentId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
            )

    val attendanceHistory: StateFlow<HistoryUiState> =
        studentRepository.getAttendanceHistory(studentId)
            .map { state ->
                when (state) {
                    is UiState.Loading -> HistoryUiState.Loading
                    is UiState.Success -> HistoryUiState.Success(state.data)
                    is UiState.Error -> {
                        if (state.message.contains("404") || state.message.contains("403")) {
                            HistoryUiState.Hidden
                        } else {
                            HistoryUiState.Error(state.message)
                        }
                    }
                    else -> HistoryUiState.Loading
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = HistoryUiState.Loading
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