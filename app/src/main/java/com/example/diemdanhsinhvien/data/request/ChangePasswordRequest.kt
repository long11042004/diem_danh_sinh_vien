package com.example.diemdanhsinhvien.data.request

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)