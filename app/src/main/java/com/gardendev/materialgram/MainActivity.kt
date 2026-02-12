package com.gardendev.materialgram

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.gardendev.materialgram.TelegramClient.Telegram.client
import com.gardendev.materialgram.ui.components.materialgram.chats.ChatListScreen
import com.gardendev.materialgram.ui.components.materialgram.events.TelegramEvents
import com.gardendev.materialgram.ui.theme.MaterialGramTheme
import org.drinkless.tdlib.TdApi

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialGramTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ChatListPage(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        TelegramClient.Telegram.initClient { update ->
            TelegramEvents.emit(update)
            if (update is TdApi.UpdateAuthorizationState) {
                runOnUiThread {
                    handleAuthState(update.authorizationState)
                }
            }
            else if (update is TdApi.UpdateFile) {
                val file = update.file
                if (file.local.isDownloadingCompleted && !file.local.path.isNullOrEmpty()) {
                    //asd
                }
            }
        }
    }

    private fun handleAuthState(state: TdApi.AuthorizationState) {
        when (state) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> {
                val params = TdApi.SetTdlibParameters().apply {
                    databaseDirectory = filesDir.absolutePath + "/tdlib"
                    apiId = 35172397
                    apiHash = "b9624baa26c8cdc635dcf5d28cb2bfee"
                    useMessageDatabase = true
                    systemLanguageCode = "ru"
                    deviceModel = "Android"
                    applicationVersion = "1.0"
                    useMessageDatabase = true
                    useChatInfoDatabase = true
                    useFileDatabase = true
                }
                TelegramClient.Telegram.client?.send(params) { }
            }
            is TdApi.AuthorizationStateWaitPhoneNumber -> {
                runOnUiThread {
                    val intent = Intent(this, StartActivity::class.java)
                    startActivity(intent)
                }
            }
            is TdApi.AuthorizationStateReady -> {
                Log.d("TDLib", "Пользователь авторизован!")
            }
        }
    }
}

@Composable
fun ChatListPage(modifier: Modifier = Modifier) {
    val chats = remember { mutableStateListOf<ChatItem>() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        // 1. Сначала просим TDLib загрузить чаты в память (кэш)
        client?.send(TdApi.LoadChats(TdApi.ChatListMain(), 100)) { result ->
            if (result is TdApi.Ok || result is TdApi.Error) {
                // Даже если ошибка (например, уже загружены), пробуем получить список

                // 2. Теперь запрашиваем список ID чатов
                client?.send(TdApi.GetChats(TdApi.ChatListMain(), 100)) { chatsResult ->
                    if (chatsResult is TdApi.Chats) {
                        chatsResult.chatIds.forEach { chatId ->
                            // 3. Получаем детали каждого чата (теперь это будет мгновенно из кэша)
                            client?.send(TdApi.GetChat(chatId)) { res ->
                                if (res is TdApi.Chat) {
                                    (context as? Activity)?.runOnUiThread {
                                        if (chats.none { it.data.id == res.id }) {
                                            chats.add(ChatItem(res))
                                            // Сортируем по дате
                                            chats.sortByDescending { it.data.lastMessage?.date ?: 0 }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    // ... остальной код подписки на TelegramEvents.updates

    // 2. ПОДПИСКА НА ОБНОВЛЕНИЯ (Чтобы список ожил)
    // 2. ПОДПИСКА НА ОБНОВЛЕНИЯ
    LaunchedEffect(Unit) {
        TelegramEvents.updates.collect { update ->
            when (update) {
                is TdApi.UpdateChatLastMessage -> {
                    val index = chats.indexOfFirst { it.data.id == update.chatId }
                    if (index != -1) {
                        val updatedChatItem = chats[index]
                        updatedChatItem.data.lastMessage = update.lastMessage
                        chats[index] = updatedChatItem

                        // Вызываем нашу умную сортировку
                        sortChatList(chats)
                    }
                }
                is TdApi.UpdateChatPosition -> {
                    // Это обновление приходит, когда чат закрепляют или открепляют
                    val index = chats.indexOfFirst { it.data.id == update.chatId }
                    if (index != -1) {
                        // TDLib обновляет позиции в массиве positions
                        // Для простоты мы можем просто пересортировать список
                        sortChatList(chats)
                    }
                }
            }
        }
    }

    Surface(modifier = modifier) {
        ChatListScreen(chats)
    }
}

@Preview(showBackground = true, showSystemUi = true, wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE)
@Composable
fun ChatListPagePreview() {
    MaterialGramTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            ChatListPage(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

private fun sortChatList(chats: MutableList<ChatItem>) {
    chats.sortWith(
        compareByDescending<ChatItem> { item ->
            // 1. Сначала проверяем, закреплен ли чат
            isChatPinned(item.data)
        }.thenByDescending { item ->
            // 2. Затем сортируем по дате последнего сообщения
            item.data.lastMessage?.date ?: 0
        }
    )
}

private fun isChatPinned(chat: TdApi.Chat): Boolean {
    // Проверяем все позиции чата, есть ли среди них закреп в главном списке
    return chat.positions.any { it.list is TdApi.ChatListMain && it.isPinned }
}