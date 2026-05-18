package com.example.chatly.ui.ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatly.R
import com.example.chatly.data.model.AiChatMessage
import io.noties.markwon.Markwon

class AiChatAdapter(private val markwon: Markwon) : 
    ListAdapter<AiChatMessage, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_AI = 1

        private object DiffCallback : DiffUtil.ItemCallback<AiChatMessage>() {
            override fun areItemsTheSame(oldItem: AiChatMessage, newItem: AiChatMessage): Boolean = 
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: AiChatMessage, newItem: AiChatMessage): Boolean = 
                oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).role == AiChatMessage.ROLE_USER) VIEW_TYPE_USER else VIEW_TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER) {
            val view = inflater.inflate(R.layout.item_chat_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_chat_ai, parent, false)
            AiViewHolder(view, markwon)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is UserViewHolder) {
            holder.bind(message)
        } else if (holder is AiViewHolder) {
            holder.bind(message)
        }
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvUserMessage)
        fun bind(message: AiChatMessage) {
            tvMessage.text = message.message
        }
    }

    class AiViewHolder(itemView: View, private val markwon: Markwon) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvAiMessage)
        fun bind(message: AiChatMessage) {
            if (message.isLoading) {
                tvMessage.text = "AI đang suy nghĩ..."
                tvMessage.alpha = 0.5f
            } else if (message.isError) {
                tvMessage.text = message.message
                tvMessage.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                tvMessage.alpha = 1.0f
            } else {
                markwon.setMarkdown(tvMessage, message.message)
                tvMessage.alpha = 1.0f
                tvMessage.setTextColor(itemView.context.getColor(android.R.color.black))
            }
        }
    }
}
