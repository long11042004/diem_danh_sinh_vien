package com.example.diemdanhsinhvien.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.database.entities.Student

enum class AttendanceStatus {
    PRESENT, ABSENT, LATE
}

class AttendanceAdapter : ListAdapter<Student, AttendanceAdapter.AttendanceViewHolder>(StudentDiffCallback) {

    private val attendanceState = mutableMapOf<Int, AttendanceStatus>()

    class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val studentNameTextView: TextView = itemView.findViewById(R.id.textViewStudentName)
        private val studentIdTextView: TextView = itemView.findViewById(R.id.textViewStudentId)
        private val studentAvatarImageView: ImageView = itemView.findViewById(R.id.imageViewAvatar)
        private val statusRadioGroup: RadioGroup = itemView.findViewById(R.id.radioGroupStatus)
        private val presentRadioButton: RadioButton = itemView.findViewById(R.id.radioPresent)
        private val absentRadioButton: RadioButton = itemView.findViewById(R.id.radioAbsent)
        private val lateRadioButton: RadioButton = itemView.findViewById(R.id.radioLate)

        fun bind(student: Student, status: AttendanceStatus, onStatusChanged: (AttendanceStatus) -> Unit) {
            studentNameTextView.text = student.studentName
            studentIdTextView.text = student.studentId
            studentAvatarImageView.setImageResource(R.drawable.ic_student_avatar)

            statusRadioGroup.setOnCheckedChangeListener(null)
            when (status) {
                AttendanceStatus.PRESENT -> presentRadioButton.isChecked = true
                AttendanceStatus.ABSENT -> absentRadioButton.isChecked = true
                AttendanceStatus.LATE -> lateRadioButton.isChecked = true
            }
            statusRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                val newStatus = when (checkedId) {
                    R.id.radioPresent -> AttendanceStatus.PRESENT
                    R.id.radioAbsent -> AttendanceStatus.ABSENT
                    R.id.radioLate -> AttendanceStatus.LATE
                    else -> AttendanceStatus.PRESENT // Mặc định
                }
                onStatusChanged(newStatus)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.attendance_item, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val student = getItem(position)
        val currentStatus = attendanceState[student.id] ?: AttendanceStatus.PRESENT
        holder.bind(student, currentStatus) { newStatus ->
            attendanceState[student.id] = newStatus
        }
    }

    fun getAttendanceResults(): Map<Int, AttendanceStatus> {
        currentList.forEach { student ->
            if (!attendanceState.containsKey(student.id)) {
                attendanceState[student.id] = AttendanceStatus.PRESENT
            }
        }
        return attendanceState
    }

    object StudentDiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean = oldItem == newItem
    }
}