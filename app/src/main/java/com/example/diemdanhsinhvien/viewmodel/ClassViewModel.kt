package com.example.diemdanhsinhvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.diemdanhsinhvien.common.UiState
import androidx.lifecycle.viewModelScope
import com.example.diemdanhsinhvien.data.model.Class as Course
import com.example.diemdanhsinhvien.data.relations.ClassWithStudentCount
import android.util.Log
import com.example.diemdanhsinhvien.repository.ClassRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClassViewModel(private val classRepository: ClassRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _refreshTrigger = MutableStateFlow(0)

    private val _classesUiState: StateFlow<UiState<List<ClassWithStudentCount>>> =
        _refreshTrigger.flatMapLatest {
            classRepository.getClassesWithStudentCount().onEach { uiState ->
                Log.d("ClassViewModel", "New UI State received: $uiState")
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    private val allClasses: Flow<List<ClassWithStudentCount>> = _classesUiState.map {
        (it as? UiState.Success)?.data ?: emptyList()
    }

    val isLoading: StateFlow<Boolean> = _classesUiState.map { it is UiState.Loading }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isSourceClassListEmpty: StateFlow<Boolean> = allClasses
        .map { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val filteredClasses: StateFlow<List<ClassWithStudentCount>> = combine(
        allClasses, _searchQuery
    ) { classes, query ->
        classes.filter { classWithCount ->
            if (query.isBlank()) {
                true
            } else {
                classWithCount.courseName?.contains(query, ignoreCase = true) == true ||
                classWithCount.courseId?.contains(query, ignoreCase = true) == true ||
                classWithCount.classCode?.contains(query, ignoreCase = true) == true
            }
        }
        .also { Log.d("ClassViewModel", "Filtered classes: ${it.size}, Query: ${query}") }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val errorMessage: StateFlow<String?> = _classesUiState.map { state ->
        (state as? UiState.Error)?.message
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun refreshClasses() {
        _refreshTrigger.value++
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