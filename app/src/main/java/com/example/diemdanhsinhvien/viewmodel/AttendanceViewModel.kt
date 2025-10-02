package com.example.diemdanhsinhvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diemdanhsinhvien.adapter.AttendanceStatus
import com.example.diemdanhsinhvien.repository.AttendanceRepository
import kotlinx.coroutines.launch

class AttendanceViewModel(private val attendanceRepository: AttendanceRepository) : ViewModel() {

    fun saveAttendance(classId: Int, attendanceData: Map<Int, AttendanceStatus>) = viewModelScope.launch {
        attendanceRepository.saveAttendance(classId, attendanceData)
    }
}

class AttendanceViewModelFactory(private val attendanceRepository: AttendanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AttendanceViewModel(attendanceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}