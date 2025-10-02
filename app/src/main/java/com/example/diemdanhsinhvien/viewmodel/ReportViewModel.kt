package com.example.diemdanhsinhvien.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.diemdanhsinhvien.model.Report
import com.example.diemdanhsinhvien.repository.ReportRepository
import kotlinx.coroutines.Dispatchers

class ReportViewModel(repository: ReportRepository) : ViewModel() {
    val reports: LiveData<List<Report>> = repository.getReports().asLiveData(Dispatchers.IO)
}