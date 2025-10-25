package com.example.diemdanhsinhvien.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SearchCategory {
    STUDENTS, LECTURERS, CLASSES
}

sealed class SearchResult {
    data class StudentResult(val id: String, val name: String, val studentId: String) : SearchResult()
    data class LecturerResult(val id: String, val name: String, val lecturerId: String) : SearchResult()
    data class ClassResult(val id: String, val className: String, val courseId: String) : SearchResult()
}
class SearchViewModel : ViewModel() {

    private val _searchResults = MutableLiveData<List<SearchResult>>()
    val searchResults: LiveData<List<SearchResult>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _noResults = MutableLiveData<Boolean>()
    val noResults: LiveData<Boolean> = _noResults

    private var searchJob: Job? = null
    private val debouncePeriod: Long = 500L

    fun search(query: String, category: SearchCategory) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _noResults.value = false
            _isLoading.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(debouncePeriod)
            _isLoading.value = true
            _noResults.value = false
            _searchResults.value = emptyList()
            val results = when (category) {
                SearchCategory.STUDENTS -> searchStudents(query)
                SearchCategory.LECTURERS -> searchLecturers(query)
                SearchCategory.CLASSES -> searchClasses(query)
            }

            _isLoading.value = false
            if (results.isEmpty()) {
                _noResults.value = true
            } else {
                _searchResults.value = results
            }
        }
    }

    private fun searchStudents(query: String): List<SearchResult> {
        val allStudents = listOf(
            SearchResult.StudentResult("1", "Nguyễn Văn A", "20201111"),
            SearchResult.StudentResult("2", "Trần Thị B", "20202222"),
            SearchResult.StudentResult("3", "Lê Văn C", "20213333")
        )
        return allStudents.filter {
            it.name.contains(query, ignoreCase = true) || it.studentId.contains(query, ignoreCase = true)
        }
    }

    private fun searchLecturers(query: String): List<SearchResult> {
        val allLecturers = listOf(
            SearchResult.LecturerResult("1", "GV. Phan Thanh An", "GV001"),
            SearchResult.LecturerResult("2", "GV. Đỗ Mỹ Linh", "GV002")
        )
        return allLecturers.filter {
            it.name.contains(query, ignoreCase = true) || it.lecturerId.contains(query, ignoreCase = true)
        }
    }

    private fun searchClasses(query: String): List<SearchResult> {
        val allClasses = listOf(
            SearchResult.ClassResult("1", "Cấu trúc dữ liệu & Giải thuật", "IT4409"),
            SearchResult.ClassResult("2", "Lập trình hướng đối tượng", "IT3100"),
            SearchResult.ClassResult("3", "Mạng máy tính", "IT3080")
        )
        return allClasses.filter {
            it.className.contains(query, ignoreCase = true) || it.courseId.contains(query, ignoreCase = true)
        }
    }
}
