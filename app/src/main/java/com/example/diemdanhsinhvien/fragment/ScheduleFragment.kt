package com.example.diemdanhsinhvien.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import com.example.diemdanhsinhvien.R

class ScheduleFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var selectedDateTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView)
        selectedDateTextView = view.findViewById(R.id.textViewSelectedDateSchedule)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // `month` là 0-based, nên cần +1 để hiển thị
            val selectedDate = "${dayOfMonth}/${month + 1}/${year}"
            val message = "Đã chọn ngày: $selectedDate"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

            // Cập nhật TextView
            selectedDateTextView.text = "Lịch học cho ngày $selectedDate"

            // TODO: Gọi ViewModel để lấy dữ liệu lịch học cho ngày được chọn
            // và cập nhật RecyclerView
        }
    }
}