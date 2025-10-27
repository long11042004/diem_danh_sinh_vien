package com.example.diemdanhsinhvien.data.search

import com.example.diemdanhsinhvien.data.model.Account
import com.example.diemdanhsinhvien.data.model.Class
import com.example.diemdanhsinhvien.data.model.Student

sealed class SearchResult {
    data class StudentResult(val student: Student) : SearchResult()
    data class ClassResult(val classItem: Class) : SearchResult()
    data class LecturerResult(val lecturer: Account) : SearchResult()
}