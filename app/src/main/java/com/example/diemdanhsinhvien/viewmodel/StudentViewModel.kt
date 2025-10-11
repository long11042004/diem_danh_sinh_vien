package com.example.diemdanhsinhvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diemdanhsinhvien.data.model.Class as Course
import com.example.diemdanhsinhvien.data.model.Student
import com.example.diemdanhsinhvien.repository.ClassRepository
import com.example.diemdanhsinhvien.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

enum class SortOrder {
    BY_NAME, BY_ID
}

@OptIn(ExperimentalCoroutinesApi::class)
class StudentViewModel(
    private val studentRepository: StudentRepository,
    private val classRepository: ClassRepository,
    private val classId: Int
) : ViewModel() {
    private val _sortOrder = MutableStateFlow(SortOrder.BY_NAME)
    private val _searchQuery = MutableStateFlow("")

    private val studentsFromRepo: Flow<List<Student>> = _sortOrder
        .flatMapLatest { sortOrder ->
            studentRepository.getStudentsForClass(classId, sortOrder)
        }

    val classDetails: StateFlow<Course?> = classRepository.getClassById(classId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val totalStudentCount: StateFlow<Int> = studentsFromRepo
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val isSourceStudentListEmpty: StateFlow<Boolean> = studentsFromRepo
        .map { it.isEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true // Giả định là trống lúc đầu
        )

    val students: StateFlow<List<Student>> = combine(
        studentsFromRepo,
        _searchQuery
    ) { students, query ->
        if (query.isBlank()) {
            students
        } else {
            students.filter {
                it.studentName?.contains(query, ignoreCase = true) == true ||
                it.studentId?.contains(query, ignoreCase = true) == true
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    fun addNewStudent(studentName: String, studentId: String, classId: Int) = viewModelScope.launch {
        val newStudent = Student(id= (1000..999999).random(), studentName = studentName, studentId = studentId, classId = classId)
        studentRepository.insertStudent(newStudent)
    }

    fun deleteStudent(student: Student) = viewModelScope.launch {
        studentRepository.deleteStudent(student)
    }

    fun reinsertStudent(student: Student) = viewModelScope.launch {
        studentRepository.insertStudent(student)
    }

    fun changeSortOrder(newSortOrder: SortOrder) {
        _sortOrder.value = newSortOrder
    }

    // Hàm để cập nhật chuỗi tìm kiếm từ UI
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}

class StudentViewModelFactory(
    private val studentRepository: StudentRepository,
    private val classRepository: ClassRepository,
    private val classId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentViewModel(studentRepository, classRepository, classId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}