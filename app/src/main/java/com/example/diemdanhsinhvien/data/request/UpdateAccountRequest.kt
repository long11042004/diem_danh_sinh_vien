package com.example.diemdanhsinhvien.data.request

data class UpdateAccountRequest(
    val fullName: String,
    val department: String?,
    val title: String?,
    val phoneNumber: String?,
    val email: String?,
    val dateOfBirth: String? // Định dạng: "YYYY-MM-DD"
)
