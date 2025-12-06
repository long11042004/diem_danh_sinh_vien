package com.example.diemdanhsinhvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.model.Notification
import com.example.diemdanhsinhvien.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<UiState<List<Notification>>>(UiState.Loading)
    val notifications: StateFlow<UiState<List<Notification>>> = _notifications.asStateFlow()

    init {
        // Lấy danh sách thông báo khi ViewModel được khởi tạo
        fetchNotifications()
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            repository.getNotifications()
                .catch { e -> _notifications.value = UiState.Error(e.message ?: "An unknown error occurred") }
                .collect { state -> _notifications.value = state }
        }
    }

    fun markNotificationAsRead(notification: Notification) {
        viewModelScope.launch {
            // Giả sử model Notification có thuộc tính 'id' kiểu String
            val result = repository.markNotificationAsRead(notification.id)
            // Nếu hoạt động thành công, làm mới danh sách để phản ánh thay đổi
            if (result is UiState.Success) {
                fetchNotifications()
            }
        }
    }

    fun createNotification(notification: Notification) {
        viewModelScope.launch {
            val result = repository.createNotification(notification)
            if (result is UiState.Success) {
                // Tải lại danh sách để hiển thị thông báo mới
                fetchNotifications()
            }
        }
    }
}

class NotificationViewModelFactory(
    private val repository: NotificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
