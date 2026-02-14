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
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toLong
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
import com.gardendev.materialgram.UserInfoPage
import com.gardendev.materialgram.utils.downloadFile
import com.gardendev.materialgram.utils.formatDate
import com.gardendev.materialgram.utils.getChatPic
import com.gardendev.materialgram.utils.getMe
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.jvm.java

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

        getChatPic(chat, 52.dp)

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chatName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
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
fun DrawerContent(onItemClick: () -> Unit) {
    var user by remember { mutableStateOf<TdApi.User?>(null) }
    LaunchedEffect(Unit) { user = getMe() }
    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),

        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                LaunchedEffect(user?.profilePhoto?.id) {
                    downloadFile(user?.profilePhoto?.small?.id)
                }
                AsyncImage(
                    model = user?.profilePhoto?.small?.local?.path,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "${user?.firstName} ${if (!user?.lastName.isNullOrEmpty()) user?.lastName else ""}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "@${user?.usernames?.activeUsernames[0]}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        NavigationDrawerItem(
            label = { Text("Profile") },
            selected = false,
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            onClick = {
                val intent = Intent(context, UserInfoPage::class.java).apply {
                    putExtra("USER_ID", user!!.id)
                }
                context.startActivity(intent)
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("New group") },
            selected = false,
            icon = { Icon(Icons.Default.Group, contentDescription = null) },
            onClick = { onItemClick() },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("Contacts") },
            selected = false,
            icon = { Icon(Icons.Default.Contacts, contentDescription = null) },
            onClick = { onItemClick() },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("Settings") },
            selected = false,
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            onClick = { onItemClick() },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(chats: List<ChatItem>) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                DrawerContent(onItemClick = { scope.launch { drawerState.close() } })
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "MaterialGram",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Menu")
                        }
                    },
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(16.dp)
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