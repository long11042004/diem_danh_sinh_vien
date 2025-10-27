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
import com.example.diemdanhsinhvien.data.model.Account
import com.example.diemdanhsinhvien.data.model.Class
import com.example.diemdanhsinhvien.data.search.SearchResult
import com.example.diemdanhsinhvien.data.model.Student

class SearchAdapter(private val onItemClicked: (SearchResult) -> Unit) :
    ListAdapter<SearchResult, RecyclerView.ViewHolder>(SearchDiffCallback()) {

    companion object {
        private const val TYPE_STUDENT = 1
        private const val TYPE_CLASS = 2
        private const val TYPE_LECTURER = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SearchResult.StudentResult -> TYPE_STUDENT
            is SearchResult.ClassResult -> TYPE_CLASS
            is SearchResult.LecturerResult -> TYPE_LECTURER
            else -> throw IllegalArgumentException("Invalid type of data at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_STUDENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.student_item, parent, false)
                StudentViewHolder(view, onItemClicked)
            }
            TYPE_CLASS -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.class_item, parent, false)
                ClassViewHolder(view, onItemClicked)
            }
            TYPE_LECTURER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.lecturer_item, parent, false)
                LecturerViewHolder(view, onItemClicked)
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            when (holder) {
                is StudentViewHolder -> holder.bind((it as SearchResult.StudentResult).student)
                is ClassViewHolder -> holder.bind((it as SearchResult.ClassResult).classItem)
                is LecturerViewHolder -> holder.bind((it as SearchResult.LecturerResult).lecturer)
            }
        }
    }

    class StudentViewHolder(itemView: View, private val onItemClicked: (SearchResult) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val studentName: TextView = itemView.findViewById(R.id.textViewStudentName)
        private val studentId: TextView = itemView.findViewById(R.id.textViewStudentId)
        private val studentAvatar: ImageView = itemView.findViewById(R.id.imageViewAvatar)

        fun bind(student: Student) {
            studentName.text = student.studentName
            studentId.text = "MSSV: ${student.studentId}"

            // Bạn có thể dùng thư viện như Glide hoặc Picasso để tải ảnh từ URL
            studentAvatar.setImageResource(R.drawable.ic_student_avatar)

            itemView.setOnClickListener {
                onItemClicked(SearchResult.StudentResult(student))
            }
        }
    }

    class ClassViewHolder(itemView: View, private val onItemClicked: (SearchResult) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val courseName: TextView = itemView.findViewById(R.id.textViewCourseName)
        private val courseId: TextView = itemView.findViewById(R.id.textViewCourseId)
        private val classCode: TextView = itemView.findViewById(R.id.textViewClassCode)
        private val semester: TextView = itemView.findViewById(R.id.textViewSemester)
        private val scheduleInfo: TextView = itemView.findViewById(R.id.textViewScheduleInfo)
        private val studentCount: TextView = itemView.findViewById(R.id.textViewStudentCount)
        private val menuButton: View = itemView.findViewById(R.id.buttonMenu)

        fun bind(classItem: Class) {
            courseName.text = classItem.courseName
            courseId.text = "Mã HP: ${classItem.courseId}"
            classCode.text = "Mã lớp: ${classItem.classCode}"
            semester.text = "Kỳ học: ${classItem.semester}"
            scheduleInfo.text = classItem.scheduleInfo

            studentCount.visibility = View.GONE
            menuButton.visibility = View.GONE

            itemView.setOnClickListener {
                onItemClicked(SearchResult.ClassResult(classItem))
            }
        }
    }

    class LecturerViewHolder(itemView: View, private val onItemClicked: (SearchResult) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        // Lưu ý: ID trong lecturer_item.xml là textViewStudentName và textViewStudentId
        private val lecturerName: TextView = itemView.findViewById(R.id.textViewStudentName)
        private val lecturerInfo: TextView = itemView.findViewById(R.id.textViewStudentId)
        private val lecturerAvatar: ImageView = itemView.findViewById(R.id.imageViewAvatar)

        fun bind(lecturer: Account) {
            lecturerName.text = lecturer.fullName
            lecturerInfo.text = "Email: ${lecturer.email}"

            // Sử dụng avatar mặc định cho giảng viên
            lecturerAvatar.setImageResource(R.drawable.ic_account)

            itemView.setOnClickListener {
                onItemClicked(SearchResult.LecturerResult(lecturer))
            }
        }
    }
}

class SearchDiffCallback : DiffUtil.ItemCallback<SearchResult>() {
    override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
        return when {
            oldItem is SearchResult.StudentResult && newItem is SearchResult.StudentResult ->
                oldItem.student.id == newItem.student.id
            oldItem is SearchResult.ClassResult && newItem is SearchResult.ClassResult ->
                oldItem.classItem.id == newItem.classItem.id
            oldItem is SearchResult.LecturerResult && newItem is SearchResult.LecturerResult ->
                oldItem.lecturer.id == newItem.lecturer.id
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
        return when {
            oldItem is SearchResult.StudentResult && newItem is SearchResult.StudentResult ->
                oldItem.student == newItem.student
            oldItem is SearchResult.ClassResult && newItem is SearchResult.ClassResult ->
                oldItem.classItem == newItem.classItem
            oldItem is SearchResult.LecturerResult && newItem is SearchResult.LecturerResult ->
                oldItem.lecturer == newItem.lecturer
            else -> false
        }
    }
}
