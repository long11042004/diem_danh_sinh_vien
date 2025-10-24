package com.example.diemdanhsinhvien.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.diemdanhsinhvien.R
import com.example.diemdanhsinhvien.adapter.NotificationAdapter
import com.example.diemdanhsinhvien.common.UiState
import com.example.diemdanhsinhvien.repository.NotificationRepository
import com.example.diemdanhsinhvien.viewmodel.NotificationViewModel
import com.example.diemdanhsinhvien.viewmodel.NotificationViewModelFactory
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private val viewModel: NotificationViewModel by viewModels {
        NotificationViewModelFactory(NotificationRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewNotifications)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarNotifications)
        val noNotificationsTextView = view.findViewById<TextView>(R.id.textViewNoNotifications)
        val adapter = NotificationAdapter { notification ->
            viewModel.markNotificationAsRead(notification)
        }

        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notifications.collect { state ->
                    progressBar.isVisible = state is UiState.Loading
                    recyclerView.isVisible = state is UiState.Success && state.data.isNotEmpty()
                    noNotificationsTextView.isVisible = state is UiState.Success && state.data.isEmpty()

                    if (state is UiState.Success) {
                        adapter.submitList(state.data)
                    }
                }
            }
        }
    }
}
