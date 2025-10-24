package com.example.diemdanhsinhvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.model.ClassReportDetail
import com.example.diemdanhsinhvien.repository.ReportRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ClassReportDetailViewModel(
    repository: ReportRepository,
    classId: Int
) : ViewModel() {

    val reportDetails: StateFlow<UiState<List<ClassReportDetail>>> =
        repository.getReportDetailsForClass(classId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
            )
}

class ClassReportDetailViewModelFactory(
    private val repository: ReportRepository,
    private val classId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClassReportDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClassReportDetailViewModel(repository, classId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
