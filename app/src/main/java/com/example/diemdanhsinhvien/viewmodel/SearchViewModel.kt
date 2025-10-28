package com.example.diemdanhsinhvien.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diemdanhsinhvien.data.search.SearchResult
import com.example.diemdanhsinhvien.data.search.SearchCategory
import com.example.diemdanhsinhvien.repository.SearchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(private val searchRepository: SearchRepository) : ViewModel() {

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 300L
    }

    private val _searchResults = MutableLiveData<List<SearchResult>>()
    val searchResults: LiveData<List<SearchResult>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _noResults = MutableLiveData<Boolean>()
    val noResults: LiveData<Boolean> = _noResults

    private var searchJob: Job? = null

    fun search(query: String, category: SearchCategory) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isBlank()) {
                _isLoading.value = false
                _noResults.value = false
                _searchResults.value = emptyList()
                return@launch
            }

            _isLoading.value = true
            _noResults.value = false // Reset cờ không có kết quả

            delay(SEARCH_DEBOUNCE_DELAY)

            val results: List<SearchResult> = when (category) {
                SearchCategory.STUDENTS -> {
                    val studentResult = searchRepository.searchStudents(query)
                    studentResult.getOrNull()?.map { SearchResult.StudentResult(it) } ?: emptyList()
                }

                SearchCategory.LECTURERS -> {
                    val lecturerResult = searchRepository.searchLecturers(query)
                    lecturerResult.getOrNull()?.map { SearchResult.LecturerResult(it) } ?: emptyList()
                }
                SearchCategory.CLASSES -> {
                    val classResult = searchRepository.searchClasses(query)
                    classResult.getOrNull()?.map { SearchResult.ClassResult(it) } ?: emptyList()
                }
            }

            _isLoading.value = false
            _noResults.value = results.isEmpty()
            _searchResults.value = results
        }
    }
}

class SearchViewModelFactory(private val repository: SearchRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
