package com.example.diemdanhsinhvien.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import android.widget.TextView
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.diemdanhsinhvien.database.entities.Class as ClassEntity
import com.example.diemdanhsinhvien.fragment.HomeFragment
import com.example.diemdanhsinhvien.repository.ClassRepository
import com.example.diemdanhsinhvien.viewmodel.ClassViewModel
import com.example.diemdanhsinhvien.viewmodel.ClassViewModelFactory
import com.example.diemdanhsinhvien.fragment.AccountFragment
import com.example.diemdanhsinhvien.fragment.ReportsFragment
import com.example.diemdanhsinhvien.fragment.ScheduleFragment
import com.example.diemdanhsinhvien.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private val classViewModel: ClassViewModel by viewModels {
        ClassViewModelFactory(ClassRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)

        val fabAddClass = findViewById<FloatingActionButton>(R.id.fab_add_class)
        fabAddClass.setOnClickListener {
            showAddClassDialog()
        }

        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNavView.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            val title: String

            when (item.itemId) {
                R.id.navigation_home -> {
                    selectedFragment = HomeFragment()
                    title = getString(R.string.title_home)
                    fabAddClass.show()
                }
                R.id.navigation_schedule -> {
                    selectedFragment = ScheduleFragment()
                    title = getString(R.string.title_schedule)
                    fabAddClass.hide()
                }
                R.id.navigation_reports -> {
                    selectedFragment = ReportsFragment()
                    title = getString(R.string.title_reports)
                    fabAddClass.hide()
                }
                R.id.navigation_account -> {
                    selectedFragment = AccountFragment()
                    title = getString(R.string.title_account)
                    fabAddClass.hide()
                }
                else -> {
                    fabAddClass.hide()
                    return@setOnItemSelectedListener false
                }
            }

            if (selectedFragment != null) {
                toolbarTitle.text = title
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit()
            }
            true
        }

        if (savedInstanceState == null) {
            bottomNavView.selectedItemId = R.id.navigation_home
        }
    }

    private fun showAddClassDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_class, null)
        val courseNameEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextCourseName)
        val courseIdEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextCourseId)
        val classCodeEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextClassCode)
        val semesterEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextSemester)
        val scheduleInfoEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextScheduleInfo)

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.add_class_dialog_title)
            .setView(dialogView)
            .setPositiveButton(R.string.add) { _, _ ->
                val courseName = courseNameEditText.text.toString().trim()
                val courseId = courseIdEditText.text.toString().trim()
                val classCode = classCodeEditText.text.toString().trim()
                val semester = semesterEditText.text.toString().trim()
                val scheduleInfo = scheduleInfoEditText.text.toString().trim()

                // Logic xác thực sẽ được chuyển xuống dưới để có trải nghiệm tốt hơn
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        // Ghi đè hành vi của nút "Thêm" để ngăn dialog tự đóng khi dữ liệu không hợp lệ
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val courseName = courseNameEditText.text.toString().trim()
                val courseId = courseIdEditText.text.toString().trim()
                val classCode = classCodeEditText.text.toString().trim()
                val semester = semesterEditText.text.toString().trim()
                val scheduleInfo = scheduleInfoEditText.text.toString().trim()

                var isValid = true

                // Kiểm tra từng trường và hiển thị lỗi nếu cần
                if (courseName.isEmpty()) {
                    courseNameEditText.error = getString(R.string.field_required_error)
                    isValid = false
                } else courseNameEditText.error = null

                if (courseId.isEmpty()) {
                    courseIdEditText.error = getString(R.string.field_required_error)
                    isValid = false
                } else courseIdEditText.error = null

                if (classCode.isEmpty()) {
                    classCodeEditText.error = getString(R.string.field_required_error)
                    isValid = false
                } else classCodeEditText.error = null

                if (semester.isEmpty()) {
                    semesterEditText.error = getString(R.string.field_required_error)
                    isValid = false
                } else if (semester.length != 5 || semester.toIntOrNull() == null) {
                    semesterEditText.error = getString(R.string.semester_format_error)
                    isValid = false
                } else semesterEditText.error = null

                if (scheduleInfo.isEmpty()) {
                    scheduleInfoEditText.error = getString(R.string.field_required_error)
                    isValid = false
                } else scheduleInfoEditText.error = null

                if (isValid) {
                    val newClass = ClassEntity(id=(1000..999999).random(), courseName = courseName, courseId = courseId, classCode = classCode, semester = semester, scheduleInfo = scheduleInfo)
                    classViewModel.insertClass(newClass)
                    Toast.makeText(this, "Đã thêm lớp học: $courseName", Toast.LENGTH_SHORT).show()
                    dialog.dismiss() // Chỉ đóng dialog khi dữ liệu hợp lệ
                }
            }
        }

        dialog.show()
    }

    fun showEditClassDialog(classToEdit: ClassEntity) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_class, null)
        val courseNameEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextCourseName)
        val courseIdEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextCourseId)
        val classCodeEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextClassCode)
        val semesterEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextSemester)
        val scheduleInfoEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextScheduleInfo)

        courseNameEditText.setText(classToEdit.courseName)
        courseIdEditText.setText(classToEdit.courseId)
        classCodeEditText.setText(classToEdit.classCode)
        semesterEditText.setText(classToEdit.semester)
        scheduleInfoEditText.setText(classToEdit.scheduleInfo)

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.edit_class_dialog_title)
            .setView(dialogView)
            .setPositiveButton(R.string.add) { _, _ -> }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val courseName = courseNameEditText.text.toString().trim()
                val courseId = courseIdEditText.text.toString().trim()
                val classCode = classCodeEditText.text.toString().trim()
                val semester = semesterEditText.text.toString().trim()
                val scheduleInfo = scheduleInfoEditText.text.toString().trim()

                var isValid = true

                if (courseName.isEmpty()) {
                    courseNameEditText.error = getString(R.string.field_required_error); isValid = false
                } else courseNameEditText.error = null

                if (courseId.isEmpty()) {
                    courseIdEditText.error = getString(R.string.field_required_error); isValid = false
                } else courseIdEditText.error = null

                if (classCode.isEmpty()) {
                    classCodeEditText.error = getString(R.string.field_required_error); isValid = false
                } else classCodeEditText.error = null

                if (semester.isEmpty()) {
                    semesterEditText.error = getString(R.string.field_required_error); isValid = false
                } else if (semester.length != 5 || semester.toIntOrNull() == null) {
                    semesterEditText.error = getString(R.string.semester_format_error); isValid = false
                } else semesterEditText.error = null

                if (scheduleInfo.isEmpty()) {
                    scheduleInfoEditText.error = getString(R.string.field_required_error); isValid = false
                } else scheduleInfoEditText.error = null

                if (isValid) {
                    val updatedClass = classToEdit.copy(courseName = courseName, courseId = courseId, classCode = classCode, semester = semester, scheduleInfo = scheduleInfo)
                    classViewModel.updateClass(updatedClass)
                    Toast.makeText(this, "Đã cập nhật lớp học", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }
}