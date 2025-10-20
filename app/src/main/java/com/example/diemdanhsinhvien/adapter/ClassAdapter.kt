package com.example.diemdanhsinhvien.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.data.relations.ClassWithStudentCount

class ClassAdapter(
    private val onItemClicked: (ClassWithStudentCount) -> Unit,
    private val onEditClicked: (ClassWithStudentCount) -> Unit,
    private val onDeleteClicked: (ClassWithStudentCount) -> Unit
) : ListAdapter<ClassWithStudentCount, ClassAdapter.ClassViewHolder>(DiffCallback) {

    class ClassViewHolder(
        itemView: View,
        private val onItemClicked: (ClassWithStudentCount) -> Unit,
        private val onEditClicked: (ClassWithStudentCount) -> Unit,
        private val onDeleteClicked: (ClassWithStudentCount) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val courseNameTextView: TextView = itemView.findViewById(R.id.textViewCourseName)
        private val courseIdTextView: TextView = itemView.findViewById(R.id.textViewCourseId)
        private val classCodeTextView: TextView = itemView.findViewById(R.id.textViewClassCode)
        private val semesterTextView: TextView = itemView.findViewById(R.id.textViewSemester)
        private val scheduleInfoTextView: TextView = itemView.findViewById(R.id.textViewScheduleInfo)
        private val studentCountTextView: TextView = itemView.findViewById(R.id.textViewStudentCount)
        private val menuButton: ImageButton = itemView.findViewById(R.id.buttonMenu)

        private var currentClass: ClassWithStudentCount? = null

        init {
            itemView.setOnClickListener {
                currentClass?.let { onItemClicked(it) }
            }
            menuButton.setOnClickListener { view ->
                currentClass?.let { showPopupMenu(view, it) }
            }
        }

        fun bind(classWithCount: ClassWithStudentCount) {
            currentClass = classWithCount

            if (!classWithCount.courseName.isNullOrBlank()) {
                courseNameTextView.text = classWithCount.courseName
                courseIdTextView.text = itemView.context.getString(R.string.course_id_label, classWithCount.courseId)
                classCodeTextView.text = itemView.context.getString(R.string.class_code_label, classWithCount.classCode)
                semesterTextView.text = itemView.context.getString(R.string.semester_label, classWithCount.semester)
                scheduleInfoTextView.text = classWithCount.scheduleInfo

                itemView.isClickable = true
                menuButton.isEnabled = true
                menuButton.alpha = 1.0f
            } else {
                courseNameTextView.text = itemView.context.getString(R.string.invalid_class_data)
                listOf(courseIdTextView, classCodeTextView, semesterTextView, scheduleInfoTextView, studentCountTextView).forEach { it.text = "" }
                itemView.isClickable = false
                menuButton.isEnabled = false
                menuButton.alpha = 0.5f
            }
            studentCountTextView.text = classWithCount.studentCount.toString()
        }

        private fun showPopupMenu(view: View, classItem: ClassWithStudentCount) {
            val popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.class_item_menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit_class -> {
                        onEditClicked(classItem)
                        true
                    }
                    R.id.action_delete_class -> {
                        onDeleteClicked(classItem)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.class_item, parent, false)
        return ClassViewHolder(view, onItemClicked, onEditClicked, onDeleteClicked)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object DiffCallback : DiffUtil.ItemCallback<ClassWithStudentCount>() {
        override fun areItemsTheSame(oldItem: ClassWithStudentCount, newItem: ClassWithStudentCount): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ClassWithStudentCount, newItem: ClassWithStudentCount): Boolean = oldItem == newItem
    }
}