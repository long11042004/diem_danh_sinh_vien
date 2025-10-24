package com.example.diemdanhsinhvien.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.diemdanhsinhvien.R
import com.google.android.material.card.MaterialCardView
import com.example.diemdanhsinhvien.data.model.Notification
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationAdapter(private val onNotificationClicked: (Notification) -> Unit) : 
    ListAdapter<Notification, NotificationAdapter.NotificationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.textViewNotificationTitle)
        private val message: TextView = itemView.findViewById(R.id.textViewNotificationMessage)
        private val timestamp: TextView = itemView.findViewById(R.id.textViewNotificationTimestamp)
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(notification: Notification) {
            itemView.setOnClickListener {
                onNotificationClicked(notification)
            }
            title.text = notification.title
            message.text = notification.message
            timestamp.text = dateFormat.format(notification.timestamp)

            title.typeface = Typeface.DEFAULT
            message.alpha = 1.0f

            if (notification.isRead) {
                (itemView as MaterialCardView).setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
            } else {
                (itemView as MaterialCardView).setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.light_blue))
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }
}
