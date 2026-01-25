package com.chats.kelompok1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val messages: List<Message>,
    private var currentUserId: String // Parameter yang menyebabkan error tadi
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    fun updateUserId(newId: String) {
        this.currentUserId = newId
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent, parent, false)
            SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_received, parent, false)
            ReceivedViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

        if (holder is SentViewHolder) {
            holder.messageText.text = message.text
            holder.timestampText.text = time
        } else if (holder is ReceivedViewHolder) {
            holder.senderName.text = message.displayName
            holder.messageText.text = message.text
            holder.timestampText.text = time
        }
    }

    override fun getItemCount() = messages.size

    class SentViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val messageText: TextView = v.findViewById(R.id.textViewMessage)
        val timestampText: TextView = v.findViewById(R.id.textViewTimestamp)
    }

    class ReceivedViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val senderName: TextView = v.findViewById(R.id.textViewSender)
        val messageText: TextView = v.findViewById(R.id.textViewMessage)
        val timestampText: TextView = v.findViewById(R.id.textViewTimestamp)
    }
}