package com.gardendev.materialgram

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File
import androidx.core.view.ViewCompat
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat

/*
class ChatAdapter(private var chats: List<ChatItem>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.chatIcon)
        val title: TextView = view.findViewById(R.id.chatTitle)
        val lastMessage: TextView = view.findViewById(R.id.lastmessage)
        val bgButton: ImageView = view.findViewById(R.id.chatListButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_list_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.title.text = chat.data.title
        holder.lastMessage.text = chat.data.lastMessage.content
        val context = holder.itemView.context

        Log.d("AvatarCheck", "Chat: ${chat.title}, Path: ${chat.localPath}")
        // Если файл скачан, localPath не будет пустым
        if (chat.isPinned) {
            // Извлекаем цвет ?attr/colorOnPrimary
            val color = context.getColorFromAttr(com.google.android.material.R.attr.colorOnPrimary)
            holder.bgButton.backgroundTintList = ColorStateList.valueOf(color)
        }
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
    fun Context.getColorFromAttr(@AttrRes attr: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}*/