package com.gardendev.materialgram

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class ChatAdapter(private var chats: List<ChatItem>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.chatIcon)
        val title: TextView = view.findViewById(R.id.chatTitle)
        val lastMessage: TextView = view.findViewById(R.id.lastmessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_list_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.title.text = chat.title
        holder.lastMessage.text = chat.lastMessage

        // Если файл скачан, localPath не будет пустым
        if (!chat.localPath.isNullOrEmpty()) {
            Glide.with(holder.avatar.context)
                .load(File(chat.localPath))
                .circleCrop()
                .into(holder.avatar)
        } else {
            holder.avatar.setImageResource(R.drawable.ic_launcher) // дефолтная иконка
        }
    }

    override fun getItemCount() = chats.size

    fun updateList(newList: List<ChatItem>) {
        chats = newList
        notifyDataSetChanged()
    }
}