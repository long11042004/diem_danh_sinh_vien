package com.example.diemdanhsinhvien.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.model.Report
import com.example.diemdanhsinhvien.repository.ReportRepository
import kotlinx.coroutines.Dispatchers

class ReportViewModel(repository: ReportRepository) : ViewModel() {
    val reports: LiveData<UiState<List<Report>>> = repository.getReports().asLiveData(Dispatchers.IO)
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