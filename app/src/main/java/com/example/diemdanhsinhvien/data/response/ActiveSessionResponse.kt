package com.example.diemdanhsinhvien.data.response

data class ActiveSessionResponse(
    val isActive: Boolean,
    val sessionId: Int?,
    val endTime: Long?,
    val sessionCode: String?,
    val message: String?
)