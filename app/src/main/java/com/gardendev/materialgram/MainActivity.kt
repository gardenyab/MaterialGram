package com.gardendev.materialgram

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.drinkless.tdlib.TdApi

class MainActivity : AppCompatActivity() {
    private val adapter = ChatAdapter(mutableListOf())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val recyclerView = findViewById<RecyclerView>(R.id.chatList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Инициализируем или получаем существующий клиент
        TelegramClient.Telegram.initClient { update ->
            if (update is TdApi.UpdateAuthorizationState) {
                runOnUiThread {
                    handleAuthState(update.authorizationState)
                }
            }
        }
    }

    private fun handleAuthState(state: TdApi.AuthorizationState) {
        when (state) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> {
                val params = TdApi.SetTdlibParameters().apply {
                    databaseDirectory = filesDir.absolutePath + "/tdlib"
                    apiId = 22117770
                    apiHash = "3cb28298ff881d18cd2dfab70f1e8f71"
                    useMessageDatabase = true
                    systemLanguageCode = "ru"
                    deviceModel = "Android"
                    applicationVersion = "1.0"
                }
                TelegramClient.Telegram.client?.send(params) { }
            }
            is TdApi.AuthorizationStateWaitPhoneNumber -> {
                val intent = Intent(this, RegisterPage::class.java)
                startActivity(intent)
                finish()
            }
            is TdApi.AuthorizationStateReady -> {
                Log.d("TDLib", "Пользователь авторизован!")
                loadChatsFromTDLib()
            }
        }
    }

    private fun loadChatsFromTDLib() {
        // 1. Запрашиваем 20 последних чатов из главного списка
        TelegramClient.Telegram.client?.send(TdApi.GetChats(TdApi.ChatListMain(), 20)) { result ->
            when (result) {
                is TdApi.Chats -> {
                    val chatItems = mutableListOf<ChatItem>()
                    val totalChats = result.chatIds.size

                    // 2. Проходим циклом по всем ID чатов
                    result.chatIds.forEach { chatId ->
                        TelegramClient.Telegram.client?.send(TdApi.GetChat(chatId)) { chat ->
                            if (chat is TdApi.Chat) {

                                // Формируем текст последнего сообщения
                                val lastMsgText = formatMessage(chat.lastMessage)

                                // Берем путь к фото, если оно уже скачано
                                val photoPath = chat.photo?.small?.local?.path

                                // Добавляем чат в наш список
                                chatItems.add(ChatItem(chat.id, chat.title, lastMsgText, photoPath))

                                // 3. Когда получили данные о последнем чате — обновляем экран
                                if (chatItems.size == totalChats) {
                                    runOnUiThread {
                                        // Передаем готовый список в адаптер
                                        adapter.updateList(chatItems)
                                    }
                                }
                            }
                        }
                    }
                }
                is TdApi.Error -> {
                    runOnUiThread {
                        Toast.makeText(this, "Ошибка загрузки: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun formatMessage(message: TdApi.Message?): String {
        if (message == null) return "Нет сообщений"

        // Получаем имя отправителя (упрощенно)
        val senderPrefix = if (message.isOutgoing) "Вы: " else ""

        val text = when (val content = message.content) {
            is TdApi.MessageText -> content.text.text
            is TdApi.MessagePhoto -> "🖼 Фото"
            is TdApi.MessageVideo -> "📹 Видео"
            is TdApi.MessageSticker -> "Наклейка ${content.sticker.emoji}"
            else -> "Сообщение"
        }
        return "$senderPrefix$text"
    }

    private fun downloadFile(fileId: Int) {
        TelegramClient.Telegram.client?.send(TdApi.DownloadFile(fileId, 1, 0, 0, false), null)
    }
}