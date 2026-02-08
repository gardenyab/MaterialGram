package com.gardendev.materialgram

data class ChatItem(
    val id: Long,
    val title: String,
    val lastMessage: String,
    val localPath: String? = null,
    val order: Long = 0 // Используется для сортировки чатов
)