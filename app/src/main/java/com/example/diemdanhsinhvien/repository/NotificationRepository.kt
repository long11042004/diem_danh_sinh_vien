package com.example.diemdanhsinhvien.repository

import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.data.model.Notification
import com.example.diemdanhsinhvien.network.apiservice.NotificationApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class NotificationRepository (
    private val notificationApiService: NotificationApiService
) {

    fun getNotifications(): Flow<UiState<List<Notification>>> = flow {
        emit(UiState.Loading)
        try {
            val response = notificationApiService.getNotifications()
            if (response.isSuccessful) {
                val notifications = response.body()
                if (notifications.isNullOrEmpty()) {
                    emit(UiState.Empty)
                } else {
                    emit(UiState.Success(notifications))
                }
            } else {
                emit(UiState.Error("API Error: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "An unknown error occurred"))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun markNotificationAsRead(notificationId: String): UiState<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = notificationApiService.markAsRead(notificationId)
            if (response.isSuccessful) UiState.Success(Unit) else UiState.Error("API Error: ${response.message()}")
        } catch (e: Exception) {
            UiState.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun createNotification(notification: Notification): UiState<Notification> = withContext(Dispatchers.IO) {
        try {
            val response = notificationApiService.createNotification(notification)
            if (response.isSuccessful) {
                response.body()?.let { createdNotification ->
                    UiState.Success(createdNotification)
                } ?: UiState.Error("Response body is null after creation")
            } else {
                UiState.Error("API Error: ${response.message()}")
            }
        } catch (e: Exception) {
            UiState.Error(e.message ?: "An unknown error occurred")
        }
    }
}