package com.example.diemdanhsinhvien.data.request

data class RegisterRequest(
    val loginName: String,
    val password: String,
    val teacherId: String,
    val fullName: String,
    val department: String,
    val dateOfBirth: String,  // "YYYY-MM-DD"
    val title: String,
    val email: String,
    val phoneNumber: String
)
