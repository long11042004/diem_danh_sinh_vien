package com.example.diemdanhsinhvien.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.adapter.StudentAdapter
import com.example.diemdanhsinhvien.network.apiservice.APIClient
import com.example.diemdanhsinhvien.repository.ClassRepository
import com.example.diemdanhsinhvien.repository.StudentRepository
import com.example.diemdanhsinhvien.viewmodel.SortOrder
import com.example.diemdanhsinhvien.viewmodel.StudentViewModel
import com.example.diemdanhsinhvien.viewmodel.StudentViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class StudentListActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private var classDetailsView: View? = null
    private lateinit var courseNameTextViewInCard: TextView
    private lateinit var classCodeTextViewInCard: TextView
    private lateinit var courseIdTextViewInCard: TextView
    private lateinit var semesterTextViewInCard: TextView
    private lateinit var studentCountTextViewInCard: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private lateinit var attendanceButton: Button
    private lateinit var sortButton: View
    private lateinit var searchView: SearchView

    private var classId: Int = -1
    private val studentViewModel: StudentViewModel by viewModels {
        StudentViewModelFactory(
            studentRepository = StudentRepository(
                studentApi = APIClient.studentApi(applicationContext),
                attendanceApi = APIClient.attendanceApi(applicationContext)
            ),
            classRepository = ClassRepository(
                courseApi = APIClient.courseApi(applicationContext)
            ),
            classId = classId
        )
    }

    companion object {
        const val EXTRA_CLASS_ID = "extra_class_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_list)

        classId = intent.getIntExtra(EXTRA_CLASS_ID, -1)

        titleTextView = findViewById(R.id.studentListTitle)
        recyclerView = findViewById(R.id.recyclerViewStudents)
        emptyTextView = findViewById(R.id.textViewNoStudents)
        sortButton = findViewById(R.id.buttonSort)
        searchView = findViewById(R.id.searchViewStudents)

        classDetailsView = findViewById(R.id.classDetailsCard)

        classDetailsView?.let { cardView ->
            courseNameTextViewInCard = cardView.findViewById(R.id.textViewCourseName)
            classCodeTextViewInCard = cardView.findViewById(R.id.textViewClassCode)
            courseIdTextViewInCard = cardView.findViewById(R.id.textViewCourseId)
            semesterTextViewInCard = cardView.findViewById(R.id.textViewSemester)
            studentCountTextViewInCard = cardView.findViewById(R.id.textViewStudentCount)
            attendanceButton = cardView.findViewById(R.id.buttonStartAttendance)
        } ?: run {
            Log.e("StudentListActivity", "classDetailsCard (include layout) not found in activity_student_list.xml. Class details will not be displayed.")
        }

        if (classId == -1) {
            finish()
            return
        }

        val backButton = findViewById<View>(R.id.buttonBack)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        sortButton.setOnClickListener {
            showSortDialog()
        }

        // Xử lý sự kiện nhấn nút Điểm danh
        if (::attendanceButton.isInitialized) {
            attendanceButton.setOnClickListener {
                val intent = Intent(this, AttendanceActivity::class.java)
                intent.putExtra(AttendanceActivity.EXTRA_CLASS_ID, classId)
                startActivity(intent)
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                studentViewModel.onSearchQueryChanged(newText.orEmpty())
                return true
            }
        })

        // --- Thiết lập RecyclerView ---
        val adapter = StudentAdapter { student ->
            val intent = Intent(this, StudentDetailActivity::class.java).apply {
                putExtra(StudentDetailActivity.EXTRA_STUDENT_DB_ID, student.id)
                putExtra(StudentDetailActivity.EXTRA_STUDENT_NAME, student.studentName)
                putExtra(StudentDetailActivity.EXTRA_STUDENT_CODE, student.studentId)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        setupSwipeToDelete(recyclerView, adapter)

        val fab = findViewById<FloatingActionButton>(R.id.fab_add_student)
        fab.setOnClickListener {
            showAddStudentDialog()
        }
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    studentViewModel.students.collect { students ->
                        // Cập nhật RecyclerView và hiển thị/ẩn dựa trên kết quả (đã lọc)
                        adapter.submitList(students)
                        val hasResults = students.isNotEmpty()
                        recyclerView.isVisible = hasResults
                        emptyTextView.isVisible = !hasResults
                    }
                }

                launch {
                    studentViewModel.classDetails.collect { classDetails ->
                        if (classDetailsView != null && classDetails != null) {
                            titleTextView.text = classDetails.courseName
                            if (::courseNameTextViewInCard.isInitialized) {
                                courseNameTextViewInCard.text = classDetails.courseName
                                classCodeTextViewInCard.text = getString(R.string.class_code_label, classDetails.classCode)
                                courseIdTextViewInCard.text = getString(R.string.course_id_label, classDetails.courseId)
                                semesterTextViewInCard.text = getString(R.string.semester_label, classDetails.semester)
                            }
                        }
                    }
                }

                launch {
                    studentViewModel.totalStudentCount.collect { count ->
                        if (::studentCountTextViewInCard.isInitialized) {
                            studentCountTextViewInCard.text = count.toString()
                        }
                    }
                }

                launch {
                    studentViewModel.isSourceStudentListEmpty.collect { isSourceEmpty ->
                        // Ẩn các nút điều khiển nếu danh sách gốc trống
                        val controlsVisible = !isSourceEmpty
                        searchView.isVisible = controlsVisible
                        sortButton.isVisible = controlsVisible

                        // Cập nhật nội dung thông báo cho phù hợp
                        if (isSourceEmpty) {
                            emptyTextView.text = getString(R.string.no_students_message)
                        } else {
                            emptyTextView.text = getString(R.string.no_search_results)
                        }
                    }
                }
            }
        }
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf(
            getString(R.string.sort_by_name),
            getString(R.string.sort_by_student_id)
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.sort_students)
            .setItems(sortOptions) { dialog, which ->
                when (which) {
                    0 -> studentViewModel.changeSortOrder(SortOrder.BY_NAME)
                    1 -> studentViewModel.changeSortOrder(SortOrder.BY_ID)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun showAddStudentDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_student, null)
        val studentNameEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextStudentName)
        val studentIdEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextStudentId)

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.add_student_dialog_title)
            .setView(dialogView)
            .setPositiveButton(R.string.add) { _, _ ->
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val studentName = studentNameEditText.text.toString().trim()
                val studentId = studentIdEditText.text.toString().trim()

                var isValid = true
                if (studentName.isEmpty()) {
                    studentNameEditText.error = getString(R.string.field_required_error)
                    isValid = false
                } else {
                    studentNameEditText.error = null
                }

                if (studentId.isEmpty()) {
                    studentIdEditText.error = getString(R.string.field_required_error)
                    isValid = false
                } else {
                    studentIdEditText.error = null
                }

                if (isValid) {
                    studentViewModel.addNewStudent(studentName, studentId, classId)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun setupSwipeToDelete(recyclerView: RecyclerView, adapter: StudentAdapter) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, // không hỗ trợ kéo-thả (drag)
            ItemTouchHelper.LEFT
        ) {
            override fun onMove( 
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val student = adapter.currentList[position]
                studentViewModel.deleteStudent(student)

                Snackbar.make(findViewById(R.id.student_list_container), getString(R.string.student_deleted_message, student.studentName), Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo) {
                        studentViewModel.reinsertStudent(student)
                    }.show()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                val itemView = viewHolder.itemView
                val background = ColorDrawable(ContextCompat.getColor(this@StudentListActivity, R.color.dark_red))
                val deleteIcon = ContextCompat.getDrawable(this@StudentListActivity, R.drawable.ic_delete)

                if (dX < 0) { // Chỉ vẽ khi trượt sang trái
                    // Vẽ nền màu đỏ
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    background.draw(c)

                    // Tính toán vị trí của biểu tượng
                    val iconMargin = (itemView.height - (deleteIcon?.intrinsicHeight ?: 0)) / 2
                    val iconTop = itemView.top + iconMargin
                    val iconBottom = iconTop + (deleteIcon?.intrinsicHeight ?: 0)
                    val iconLeft = itemView.right - iconMargin - (deleteIcon?.intrinsicWidth ?: 0)
                    val iconRight = itemView.right - iconMargin

                    deleteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    deleteIcon?.draw(c)
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
    }
}
