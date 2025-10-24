package com.example.diemdanhsinhvien.data.model

import java.util.Date

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Date,
    var isRead: Boolean = false
) {
    fun copyWith(isRead: Boolean): Notification {
        return copy(id = this.id, title = this.title, message = this.message, timestamp = this.timestamp, isRead = isRead)
    }
}
