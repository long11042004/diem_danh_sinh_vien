package com.example.diemdanhsinhvien.repository

import android.util.Log
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.model.Notification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class NotificationRepository {

    companion object {
        private val mockNotifications = mutableListOf<Notification>()

        private val _notificationsFlow = MutableStateFlow(mockNotifications.toList())

        fun addNotification(notification: Notification) {
            mockNotifications.add(0, notification)
            _notificationsFlow.value = mockNotifications.toList()
        }
    }

    fun markNotificationAsRead(notification: Notification) {
        val index = mockNotifications.indexOfFirst { it.id == notification.id }
        if (index != -1) {
            val updatedNotification = notification.copyWith(isRead = true)
            mockNotifications[index] = updatedNotification
            _notificationsFlow.value = mockNotifications.toList()
            Log.d("NotificationRepository", "Notification ${notification.id} marked as read")
        } else {
            Log.d("NotificationRepository", "Notification ${notification.id} not found")
        }
    }

    fun getNotifications(): Flow<UiState<List<Notification>>> {
        return _notificationsFlow.asStateFlow().map { list ->
            UiState.Success(list.sortedByDescending { it.timestamp })
        }
    }
}
