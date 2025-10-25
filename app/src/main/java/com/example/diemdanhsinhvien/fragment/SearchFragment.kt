package com.example.diemdanhsinhvien.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.diemdanhsinhvien.R
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

    private val viewModel: SearchViewModel by viewModels()
    // Bạn sẽ cần tạo Adapter ở bước tiếp theo
    // private lateinit var searchAdapter: SearchAdapter 

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

        setupSearchInput()
        setupCategoryChips()
        observeViewModel()

        chipStudents.isChecked = true
    }

    /*
    private fun setupRecyclerView() {
        searchAdapter = SearchAdapter { searchResult ->
            // Xử lý khi click vào một item, ví dụ: chuyển đến màn hình chi tiết
            when (searchResult) {
                is SearchResult.StudentResult -> { /* Chuyển đến chi tiết sinh viên */ }
                is SearchResult.LecturerResult -> { /* Chuyển đến chi tiết giảng viên */ }
                is SearchResult.ClassResult -> { /* Chuyển đến chi tiết lớp học */ }
            }
        }
        binding.recyclerViewSearchResults.adapter = searchAdapter
    }
    */

    private fun setupSearchInput() {
        searchEditText.doOnTextChanged { text, _, _, _ ->
            performSearch()
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                // Ẩn bàn phím
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
            else -> SearchCategory.STUDENTS // Mặc định
        }
        viewModel.search(query, selectedCategory)
    }

    private fun observeViewModel() {
        /*
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchAdapter.submitList(results)
        }
        */

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.isVisible = isLoading
        }

        viewModel.noResults.observe(viewLifecycleOwner) { noResults ->
            textViewNoResults.isVisible = noResults
            recyclerViewSearchResults.isVisible = !noResults
        }
    }
}
