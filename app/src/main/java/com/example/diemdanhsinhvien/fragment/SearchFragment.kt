package com.example.diemdanhsinhvien.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.adapter.SearchAdapter
import com.example.diemdanhsinhvien.data.search.SearchResult
import com.example.diemdanhsinhvien.data.search.SearchCategory
import com.example.diemdanhsinhvien.network.apiservice.APIClient
import com.example.diemdanhsinhvien.network.apiservice.SearchApiService
import com.example.diemdanhsinhvien.repository.SearchRepository
import com.example.diemdanhsinhvien.viewmodel.SearchViewModel
import com.example.diemdanhsinhvien.viewmodel.SearchViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchFragment : Fragment() {

    private lateinit var searchEditText: TextInputEditText
    private lateinit var searchCategoryChipGroup: ChipGroup
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewNoResults: TextView
    private lateinit var recyclerViewSearchResults: RecyclerView
    private lateinit var chipStudents: Chip
    private lateinit var chipLecturers: Chip
    private lateinit var chipClasses: Chip

    private val searchApiService: SearchApiService by lazy {
        APIClient.searchApi(requireContext())
    }

    private val searchRepository: SearchRepository by lazy {
        SearchRepository(searchApiService)
    }

    private val viewModelFactory: SearchViewModelFactory by lazy {
        SearchViewModelFactory(searchRepository)
    }
    private val viewModel: SearchViewModel by viewModels { viewModelFactory }
    private lateinit var searchAdapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEditText = view.findViewById(R.id.search_edit_text)
        searchCategoryChipGroup = view.findViewById(R.id.search_category_chip_group)
        progressBar = view.findViewById(R.id.progress_bar)
        textViewNoResults = view.findViewById(R.id.text_view_no_results)
        recyclerViewSearchResults = view.findViewById(R.id.recycler_view_search_results)
        chipStudents = view.findViewById(R.id.chip_students)
        chipLecturers = view.findViewById(R.id.chip_lecturers)
        chipClasses = view.findViewById(R.id.chip_classes)

        setupRecyclerView()
        setupSearchInput()
        setupCategoryChips()
        observeViewModel()

        chipStudents.isChecked = true
    }
    
    private fun setupRecyclerView() {
        searchAdapter = SearchAdapter { searchResult ->
            when (searchResult) {
                is SearchResult.StudentResult -> {
                    // TODO: Chuyển đến màn hình chi tiết sinh viên với searchResult.student.id
                }
                is SearchResult.ClassResult -> {
                    // TODO: Chuyển đến màn hình chi tiết lớp học với searchResult.classItem.id
                }
                is SearchResult.LecturerResult -> {
                    // TODO: Chuyển đến màn hình chi tiết giảng viên với searchResult.lecturer.id
                }
            }
        }
        recyclerViewSearchResults.adapter = searchAdapter
    }

    private fun setupSearchInput() {
        searchEditText.doOnTextChanged { text, _, _, _ ->
            performSearch()
        }

        searchEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                false
            }
        }
    }
    private fun setupCategoryChips() {
        searchCategoryChipGroup.setOnCheckedChangeListener { group, checkedId ->
            performSearch()
        }
    }
    private fun performSearch() {
        val query = searchEditText.text.toString().trim()
        val selectedCategory = when (searchCategoryChipGroup.checkedChipId) {
            R.id.chip_lecturers -> SearchCategory.LECTURERS
            R.id.chip_classes -> SearchCategory.CLASSES
            else -> SearchCategory.STUDENTS
        }
        viewModel.search(query, selectedCategory)
    }

    private fun observeViewModel() {
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchAdapter.submitList(results)
        }


        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.isVisible = isLoading
            // Khi đang tải, ẩn kết quả và thông báo
            if (isLoading) {
                recyclerViewSearchResults.isVisible = false
                textViewNoResults.isVisible = false
            }
        }

        viewModel.noResults.observe(viewLifecycleOwner) { noResults ->
            // Chỉ hiển thị khi đã tải xong
            if (viewModel.isLoading.value == false) {
                textViewNoResults.isVisible = noResults
                recyclerViewSearchResults.isVisible = !noResults
            }
        }
    }
}
