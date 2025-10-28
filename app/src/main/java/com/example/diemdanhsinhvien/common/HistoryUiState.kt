package com.example.diemdanhsinhvien.common

import com.example.diemdanhsinhvien.data.relations.StudentAttendanceHistory

sealed class HistoryUiState {
    data class Success(val data: List<StudentAttendanceHistory>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
    object Loading : HistoryUiState()
    object Hidden : HistoryUiState() // Trạng thái để ẩn hoàn toàn mục lịch sử
}