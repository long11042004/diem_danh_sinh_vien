package com.example.diemdanhsinhvien.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.model.Report
import com.example.diemdanhsinhvien.repository.ReportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReportViewModel(private val reportRepository: ReportRepository) : ViewModel() {

    private val _reports = MutableLiveData<UiState<List<Report>>>()
    val reports: LiveData<UiState<List<Report>>> = _reports

    fun fetchReports(teacherId: Int) {
        viewModelScope.launch {
            _reports.value = UiState.Loading
            try {
                val reportList = reportRepository.getReportsForTeacher(teacherId)
                _reports.value = UiState.Success(reportList)
            } catch (e: Exception) {
                _reports.value = UiState.Error("Lỗi khi tải báo cáo: ${e.message}")
            }
        }
    }
}

class ReportViewModelFactory(private val repository: ReportRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}