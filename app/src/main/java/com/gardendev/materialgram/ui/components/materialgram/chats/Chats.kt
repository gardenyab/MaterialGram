package com.gardendev.materialgram.ui.components.materialgram.chats

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gardendev.materialgram.ChatItem
import com.gardendev.materialgram.ChatPage
import com.gardendev.materialgram.TelegramClient.Telegram.client
import org.drinkless.tdlib.TdApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatListItem(
    chat: TdApi.Chat,
    chatName: String,
    lastMessage: String,
    time: String,
    unreadCount: Int,
    isPinned: Boolean,
    chatId: Long
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, ChatPage::class.java).apply {
                    putExtra("CHAT_ID", chatId)
                    putExtra("CHAT_TITLE", chatName)
                }
                context.startActivity(intent)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val photo = chat.photo?.small
        val localPath = photo?.local?.path

        Box(modifier = Modifier.size(52.dp)) {
            if (!localPath.isNullOrEmpty()) {
                AsyncImage(
                    model = localPath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                LaunchedEffect(photo?.id) {
                    photo?.id?.let { client?.send(TdApi.DownloadFile(it, 1, 0, 0, true)) {} }
                }
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(chatName.take(1).uppercase(), color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chatName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface // Явный цвет
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListScreen(chats: List<ChatItem>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "MaterialGram",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            itemsIndexed(
                items = chats,
                key = { _, chat -> chat.data.id }
            ) { index, chat ->
                val shape = getListItemShape(index, chats.size)

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = shape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    ChatListItem(
                        chat = chat.data,
                        chatName = chat.data.title,
                        lastMessage = formatMessage(chat.data.lastMessage),
                        time = formatDate(chat.data.lastMessage?.date ?: 0),
                        unreadCount = chat.data.unreadCount,
                        isPinned = isChatPinned(chat.data),
                        chatId = chat.data.id
                    )
                }
            }
        }
    }
}

@Composable
fun getListItemShape(index: Int, totalCount: Int): Shape {
    val large = 20.dp
    val small = 4.dp
    return when {
        totalCount == 1 -> RoundedCornerShape(large)
        index == 0 -> RoundedCornerShape(topStart = large, topEnd = large, bottomStart = small, bottomEnd = small)
        index == totalCount - 1 -> RoundedCornerShape(topStart = small, topEnd = small, bottomStart = large, bottomEnd = large)
        else -> RoundedCornerShape(small)
    }
}

private fun isChatPinned(chat: TdApi.Chat): Boolean {
    return chat.positions.any { it.list is TdApi.ChatListMain && it.isPinned }
}

private fun formatMessage(message: TdApi.Message?): String {
    if (message == null) return "No messages"
    val prefix = if (message.isOutgoing) "You: " else ""
    val text = when (val content = message.content) {
        is TdApi.MessageText -> content.text.text
        is TdApi.MessagePhoto -> "🖼 Photo"
        is TdApi.MessageVideo -> "📹 Video"
        is TdApi.MessageSticker -> "Sticker ${content.sticker.emoji}"
        is TdApi.MessageAnimation -> "GIF"
        else -> "Message"
    }
    return "$prefix$text"
}

fun formatDate(unixTimestamp: Int): String {
    if (unixTimestamp == 0) return ""
    val date = Date(unixTimestamp.toLong() * 1000)
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(date)
}