package com.example.diemdanhsinhvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diemdanhsinhvien.database.entities.Class as Course
import com.example.diemdanhsinhvien.database.relations.ClassWithStudentCount
import com.example.diemdanhsinhvien.repository.ClassRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClassViewModel(private val classRepository: ClassRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val allClassesWithStudentCount: Flow<List<ClassWithStudentCount>> =
        classRepository.getClassesWithStudentCount()

    val isSourceClassListEmpty: StateFlow<Boolean> =
        allClassesWithStudentCount
            .map { it.isEmpty() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = true
            )

    val filteredClasses: StateFlow<List<ClassWithStudentCount>> = combine(
        allClassesWithStudentCount,
        _searchQuery
    ) { classes, query ->
        if (query.isBlank()) {
            classes
        } else {
            classes.filter { classWithCount ->
                val classInfo = classWithCount.classInfo
                classInfo.courseName.contains(query, ignoreCase = true) ||
                classInfo.courseId.contains(query, ignoreCase = true) ||
                classInfo.classCode.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun insertClass(classData: Course) = viewModelScope.launch { classRepository.insertClass(classData) }
    fun deleteClass(classData: Course) = viewModelScope.launch { classRepository.deleteClass(classData) }
    fun updateClass(classData: Course) = viewModelScope.launch { classRepository.updateClass(classData) }
}

class ClassViewModelFactory(private val classRepository: ClassRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClassViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClassViewModel(classRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}