package com.example.diemdanhsinhvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.model.ClassReportDetail
import com.example.diemdanhsinhvien.repository.ClassRepository
import com.example.diemdanhsinhvien.repository.ReportRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ClassReportDetailViewModel(
    reportRepository: ReportRepository,
    classRepository: ClassRepository,
    classId: Int
) : ViewModel() {

    val reportDetails: StateFlow<UiState<List<ClassReportDetail>>> =
        reportRepository.getReportDetailsForClass(classId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
            )

    val classDetails: StateFlow<com.example.diemdanhsinhvien.data.model.Class?> =
        classRepository.getClassById(classId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}

class ClassReportDetailViewModelFactory(
    private val reportRepository: ReportRepository,
    private val classRepository: ClassRepository,
    private val classId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClassReportDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClassReportDetailViewModel(reportRepository, classRepository, classId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
