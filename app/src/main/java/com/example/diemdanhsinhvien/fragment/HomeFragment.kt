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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import com.example.diemdanhsinhvien.activity.StudentListActivity
import com.example.diemdanhsinhvien.activity.MainActivity
import com.example.diemdanhsinhvien.repository.ClassRepository
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.adapter.ClassAdapter
import com.example.diemdanhsinhvien.viewmodel.ClassViewModel
import com.example.diemdanhsinhvien.viewmodel.ClassViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val classViewModel: ClassViewModel by activityViewModels {
        ClassViewModelFactory(
            ClassRepository()
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
        val adapter = ClassAdapter(
            onItemClicked = { classItem ->
                val intent = Intent(activity, StudentListActivity::class.java)
                intent.putExtra(StudentListActivity.EXTRA_CLASS_ID, classItem.classInfo.id)
                startActivity(intent)
            },
            onEditClicked = { classToEdit ->
                (activity as? MainActivity)?.showEditClassDialog(classToEdit.classInfo)
            },
            onDeleteClicked = { classToDelete ->
                showDeleteConfirmationDialog(classToDelete.classInfo)
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                classViewModel.onSearchQueryChanged(newText.orEmpty())
                return true
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine( 
                    classViewModel.filteredClasses,
                    classViewModel.isSourceClassListEmpty,
                    classViewModel.isLoading
                ) { filteredList, isSourceEmpty, isLoading ->
                    adapter.submitList(filteredList)

                    progressBar.isVisible = isLoading
                    searchView.isVisible = !isSourceEmpty && !isLoading

                    val showEmptyView = !isLoading && filteredList.isEmpty()
                    recyclerView.isVisible = !isLoading && !showEmptyView
                    emptyTextView.isVisible = showEmptyView

                    if (showEmptyView) {
                        emptyTextView.text = if (isSourceEmpty) getString(R.string.no_classes_message) else getString(R.string.no_search_results_class)
                    }
                }.collect{}
            }
        }
    }

    private fun showDeleteConfirmationDialog(classToDelete: com.example.diemdanhsinhvien.database.entities.Class) {
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