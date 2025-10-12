package com.example.diemdanhsinhvien.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import com.example.diemdanhsinhvien.activity.StudentListActivity
import com.example.diemdanhsinhvien.activity.MainActivity
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.network.apiservice.APIClient
import com.example.diemdanhsinhvien.repository.AccountRepository
import com.example.diemdanhsinhvien.repository.ClassRepository
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.adapter.ClassAdapter
import com.example.diemdanhsinhvien.viewmodel.AuthViewModel
import com.example.diemdanhsinhvien.viewmodel.AuthViewModelFactory
import com.example.diemdanhsinhvien.viewmodel.ClassViewModel
import com.example.diemdanhsinhvien.viewmodel.ClassViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val classViewModel: ClassViewModel by activityViewModels {
        ClassViewModelFactory(
            ClassRepository(
                courseApi = APIClient.courseApi(requireContext())
            )
        )
    }

    private val authViewModel: AuthViewModel by activityViewModels {
        AuthViewModelFactory(
            AccountRepository(
                accountApi = APIClient.accountApi(requireContext())
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewClasses)
        val emptyTextView = view.findViewById<TextView>(R.id.textViewNoClasses)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val searchView = view.findViewById<SearchView>(R.id.searchViewClasses)
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        val lecturerNameTextView = view.findViewById<TextView>(R.id.textViewLecturerName)
        val lecturerIdTextView = view.findViewById<TextView>(R.id.textViewLecturerId)
        val adapter = ClassAdapter(
            onItemClicked = { classItem ->
                val intent = Intent(activity, StudentListActivity::class.java)
                intent.putExtra(StudentListActivity.EXTRA_CLASS_ID, classItem.id)
                startActivity(intent)
            },
            onEditClicked = { classToEdit ->
                val classObject = com.example.diemdanhsinhvien.data.model.Class(
                    id = classToEdit.id,
                    courseName = classToEdit.courseName,
                    courseId = classToEdit.courseId,
                    classCode = classToEdit.classCode,
                    semester = classToEdit.semester,
                    scheduleInfo = classToEdit.scheduleInfo ?: ""
                )
                (activity as? MainActivity)?.showEditClassDialog(classObject)
            },
            onDeleteClicked = { classToDelete ->
                val classObject = com.example.diemdanhsinhvien.data.model.Class(
                    id = classToDelete.id,
                    courseName = classToDelete.courseName,
                    courseId = classToDelete.courseId,
                    classCode = classToDelete.classCode,
                    semester = classToDelete.semester,
                    scheduleInfo = classToDelete.scheduleInfo ?: ""
                )
                showDeleteConfirmationDialog(classObject)
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        swipeRefreshLayout.setOnRefreshListener {
            classViewModel.refreshClasses()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                classViewModel.onSearchQueryChanged(newText.orEmpty())
                return true
            }
        })

        authViewModel.getAccountDetails()
        setupAccountObserver(lecturerNameTextView, lecturerIdTextView)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine( 
                    classViewModel.filteredClasses,
                    classViewModel.isSourceClassListEmpty,
                    classViewModel.isLoading,
                    classViewModel.errorMessage
                ) { filteredList, isSourceEmpty, isLoading, errorMessage ->
                    adapter.submitList(filteredList)

                    swipeRefreshLayout.isRefreshing = isLoading
                    progressBar.isVisible = isLoading && filteredList.isEmpty() && errorMessage == null

                    searchView.isVisible = !isLoading && !isSourceEmpty && errorMessage == null

                    val hasError = errorMessage != null
                    val showEmptyView = !isLoading && (filteredList.isEmpty() || hasError)

                    recyclerView.isVisible = !isLoading && !showEmptyView
                    emptyTextView.isVisible = showEmptyView

                    if (showEmptyView) {
                        if (hasError) {
                            emptyTextView.text = errorMessage
                        } else {
                            emptyTextView.text = if (isSourceEmpty) getString(R.string.no_classes_message) else getString(R.string.no_search_results_class)
                        }
                    }
                    Log.d("HomeFragment", "Filtered classes: ${filteredList.size}")
                }.collect{}
            }
        }
    }

    private fun setupAccountObserver(lecturerNameTextView: TextView, lecturerIdTextView: TextView) {
        authViewModel.accountDetails.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    lecturerNameTextView.text = getString(R.string.loading)
                    lecturerIdTextView.text = ""
                }
                is UiState.Success -> {
                    state.data?.let { account ->
                        lecturerNameTextView.text = account.fullName
                        lecturerIdTextView.text = getString(R.string.lecturer_id_format, account.teacherId)
                        Log.i("HomeFragment", "Account details received: $account")
                    } ?: run {
                        lecturerNameTextView.text = getString(R.string.not_logged_in)
                        lecturerIdTextView.text = ""
                        Log.w("HomeFragment", "Account details are null.")
                    }
                }
                is UiState.Error -> {
                    lecturerNameTextView.text = getString(R.string.error_loading_data)
                    lecturerIdTextView.text = ""
                    Log.e("HomeFragment", "Error loading account details: ${state.message}")
                }
                else -> {
                    lecturerNameTextView.text = getString(R.string.not_logged_in)
                    lecturerIdTextView.text = ""
                    Log.w("HomeFragment", "Observed an unhandled or null state for accountDetails in HomeFragment.")
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(classToDelete: com.example.diemdanhsinhvien.data.model.Class) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_class_dialog_title)
            .setMessage(getString(R.string.delete_class_dialog_message, classToDelete.courseName))
            .setPositiveButton(R.string.delete) { _, _ ->
                classViewModel.deleteClass(classToDelete)
                Toast.makeText(requireContext(), "Đã xóa lớp học: ${classToDelete.courseName}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}