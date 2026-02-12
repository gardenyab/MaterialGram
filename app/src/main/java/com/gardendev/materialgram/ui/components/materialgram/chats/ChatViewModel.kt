package com.gardendev.materialgram.ui.components.materialgram.chats

import android.os.Looper
import android.os.Looper.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gardendev.materialgram.ui.components.materialgram.events.TelegramEvents
import com.gardendev.materialgram.utils.getMe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import java.util.logging.Handler

class ChatViewModel(private val client: Client) : ViewModel() {
    private val _messages = MutableStateFlow<List<TdApi.Message>>(emptyList())
    val messages: StateFlow<List<TdApi.Message>> = _messages.asStateFlow()
    private val _currentChat = MutableStateFlow<TdApi.Chat?>(null)
    val currentChat: StateFlow<TdApi.Chat?> = _currentChat.asStateFlow()
    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val users = mutableMapOf<Long, String>()
    var currentChatId: Long = 0


    init {
        viewModelScope.launch {
            TelegramEvents.updates.collect { update ->
                when (update) {
                    is TdApi.UpdateNewMessage -> {
                        if (update.message.chatId == currentChatId) {
                            _messages.update { currentList -> listOf(update.message) + currentList }
                        }
                    }
                    is TdApi.UpdateChatAction -> {
                        if (update.chatId == currentChatId) {
                            handleTypingAction(update.action)
                        }
                    }
                }
            }
        }
    }

    private fun handleTypingAction(action: TdApi.ChatAction?) {
        if (action is TdApi.ChatActionTyping) {
            _isTyping.value = true
            viewModelScope.launch {
                delay(4000)
                _isTyping.value = false
            }
        }
    }

    fun getUserName(sender: TdApi.MessageSender?, callback: (String) -> Unit) {
        if (sender is TdApi.MessageSenderUser) {
            val cached = users[sender.userId]
            if (cached != null) {
                callback(cached)
                return
            }

            client.send(TdApi.GetUser(sender.userId)) { res ->
                if (res is TdApi.User) {
                    val name = "${res.firstName} ${res.lastName}".trim()
                    users[sender.userId] = name
                    android.os.Handler(getMainLooper()).post {
                        callback(name)
                    }
                }
            }
        } else if (sender is TdApi.MessageSenderChat) {
            client.send(TdApi.GetChat(sender.chatId)) { res ->
                if (res is TdApi.Chat) {
                    android.os.Handler(getMainLooper()).post {
                        callback(res.title)
                    }
                }
            }
        }
    }

    fun sendMessage(text: String, replyToMessageId: Long = 0) {
        if (currentChatId == 0L || text.isBlank()) return

        val content = TdApi.InputMessageText(
            TdApi.FormattedText(text, null),
            null,
            true
        )

        val replyTo = if (replyToMessageId != 0L) {
            TdApi.InputMessageReplyToMessage(replyToMessageId, null, 0)
        } else null

        client.send(TdApi.SendMessage(
            currentChatId,
            0,
            replyTo,
            null,
            null,
            content
        )) { }
    }

    fun updateMessageContent(messageId: Long, newContent: TdApi.MessageContent) {
        viewModelScope.launch { // Используем корутину для безопасности потоков
            _messages.update { currentList ->
                currentList.map { msg ->
                    if (msg.id == messageId) {
                        // Вместо создания нового объекта, меняем поле у существующего
                        // (в TDLib Java объекты мутабельны)
                        msg.content = newContent
                        msg
                    } else msg
                }
            }
        }
    }
    // Внутри ChatViewModel
    fun addMessage(message: TdApi.Message) {
        if (message.chatId == currentChatId) { // Проверяем, что сообщение из этого чата
            _messages.value = listOf(message) + _messages.value
        }
    }
    fun loadHistory(chatId: Long) {
        currentChatId = chatId
        _messages.value = emptyList()
        client.send(TdApi.OpenChat(chatId)) { }

        client.send(TdApi.GetChatHistory(chatId, 0, 0, 150, false)) { res ->
            when (res) {
                is TdApi.Messages -> {
                    // В loadHistory заменяем Handler на:
                    viewModelScope.launch(Dispatchers.Main) {
                        _messages.value = res.messages.toList()
                    }
                }
                is TdApi.Error -> {
                    println("History error: ${res.message}")
                }
            }
        }
    }

    fun updateFile(file: TdApi.File) {
        // Чтобы Compose увидел изменения в путях файлов (local.path),
        // нужно создать новый список. Просто .map{it} достаточно для триггера.
        _messages.update { currentList ->
            currentList.map { it }
        }
    }

    // Вызывай это, когда пользователь выходит из экрана чата (например, в Activity.onDestroy или onBack)
    fun closeChat() {
        if (currentChatId != 0L) {
            client.send(TdApi.CloseChat(currentChatId)) { }
        }
    }

    private fun preloadMessageContent(message: TdApi.Message) {
        when (val content = message.content) {
            is TdApi.MessagePhoto -> {
                val photo = content.photo.sizes.lastOrNull()?.photo
                if (photo != null && !photo.local.isDownloadingCompleted) {
                    client.send(TdApi.DownloadFile(photo.id, 1, 0, 0, true)) {}
                }
            }
            is TdApi.MessageVideo -> {
                val thumb = content.video.thumbnail?.file
                if (thumb != null && !thumb.local.isDownloadingCompleted) {
                    client.send(TdApi.DownloadFile(thumb.id, 1, 0, 0, true)) {}
                }
            }
        }
    }
}