package com.example.diemdanhsinhvien.data.model

data class Account(
    val id: Int,
    val loginName: String,
    val teacherId: String,
    val fullName: String,
    val department: String,
    val dateOfBirth: String,
    val title: String,
    val email: String,
    val phoneNumber: String,
    val created_at: String
)