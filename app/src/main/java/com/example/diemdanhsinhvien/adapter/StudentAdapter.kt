package com.example.diemdanhsinhvien.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.data.model.Student

class StudentAdapter(private val onItemClicked: (Student) -> Unit) :
    ListAdapter<Student, StudentAdapter.StudentViewHolder>(StudentDiffCallback) {

    class StudentViewHolder(itemView: View, private val onItemClicked: (Student) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val studentNameTextView: TextView = itemView.findViewById(R.id.textViewStudentName)
        private val studentIdTextView: TextView = itemView.findViewById(R.id.textViewStudentId)
        private val studentAvatarImageView: ImageView = itemView.findViewById(R.id.imageViewAvatar)

        private var currentStudent: Student? = null

        init {
            itemView.setOnClickListener {
                currentStudent?.let { student ->
                    onItemClicked(student)
                }
            }
        }

        fun bind(student: Student) {
            currentStudent = student
            studentNameTextView.text = student.studentName
            studentIdTextView.text = student.studentId
            studentAvatarImageView.setImageResource(R.drawable.ic_student_avatar)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.student_item, parent, false)
        return StudentViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = getItem(position)
        holder.bind(student)
    }

    object StudentDiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem == newItem
        }
    }
}
