package com.gardendev.materialgram.ui.components.materialgram.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gardendev.materialgram.ui.components.materialgram.events.TelegramEvents
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi

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

    fun loadHistory(chatId: Long) {
        this.currentChatId = chatId
        client.send(TdApi.GetChat(chatId)) { res ->
            if (res is TdApi.Chat) _currentChat.value = res
        }
        client.send(TdApi.GetChatHistory(chatId, 0, 0, 50, true)) { result ->
            if (result is TdApi.Messages) {
                _messages.value = result.messages.toList()
            }
            client.send(TdApi.GetChatHistory(chatId, 0, 0, 50, false)) { freshResult ->
                if (freshResult is TdApi.Messages) {
                    _messages.value = freshResult.messages.toList()
                }
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
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        callback(name)
                    }
                }
            }
        } else if (sender is TdApi.MessageSenderChat) {
            client.send(TdApi.GetChat(sender.chatId)) { res ->
                if (res is TdApi.Chat) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
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
}