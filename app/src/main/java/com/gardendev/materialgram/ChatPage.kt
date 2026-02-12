package com.gardendev.materialgram

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gardendev.materialgram.TelegramClient.Telegram.client
import com.gardendev.materialgram.ui.components.materialgram.chats.ChatViewModel
import com.gardendev.materialgram.ui.theme.MaterialGramTheme
import com.gardendev.materialgram.utils.sendMessage
import com.gardendev.materialgram.utils.toAnnotatedString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi

class ChatPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val chatId = intent.getLongExtra("CHAT_ID", 0L)
        val chatTitle = intent.getStringExtra("CHAT_TITLE") ?: "Чат"

        setContent {
            MaterialGramTheme {
                val chatViewModel: ChatViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ChatViewModel(TelegramClient.Telegram.client!!) as T
                        }
                    }
                )

                LaunchedEffect(chatId) {
                    if (chatId != 0L) {
                        chatViewModel.loadHistory(chatId)
                    }
                }

                ChatScreen(
                    viewModel = chatViewModel,
                    chatTitle = chatTitle,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    chatTitle: String,
    onBackClick: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var highlightedMessageId by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        com.gardendev.materialgram.ui.components.materialgram.events.TelegramEvents.updates.collect { update ->
            when (update) {
                is TdApi.UpdateFile -> {
                    if (update.file.local.isDownloadingCompleted) {
                        viewModel.updateFile(update.file)
                    }
                }
                is TdApi.UpdateMessageContent -> {
                    viewModel.updateMessageContent(update.messageId, update.newContent)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        var chatAvatar by remember { mutableStateOf<String?>(null) }
                        LaunchedEffect(chatTitle) {
                            viewModel.currentChat.value?.let { chat ->
                                chatAvatar = chat.photo?.small?.local?.path
                            }
                        }

                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            if (chatAvatar != null) {
                                AsyncImage(model = chatAvatar, contentDescription = null, contentScale = ContentScale.Crop)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(chatTitle, style = MaterialTheme.typography.titleMedium, modifier = Modifier.clickable( onClick = {
                            val intent = Intent(context, UserInfoPage::class.java).apply {
                                putExtra("USER_ID", viewModel.currentChatId)
                            }
                            context.startActivity(intent)
                        }))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true,
                contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp, start = 8.dp, end = 8.dp)
            ) {
                itemsIndexed(messages, key = { _, m -> m.id }) { index, message ->
                    val isLastInBlock = if (index > 0) {
                        messages[index - 1].senderId != message.senderId
                    } else true

                    val isHighlighted = highlightedMessageId == message.id

                    MessageBubble(
                        message = message,
                        showSenderInfo = !message.isOutgoing && isLastInBlock,
                        viewModel = viewModel,
                        isHighlighted = isHighlighted,
                        onReplyClick = { replyMessageId ->
                            val targetIndex = messages.indexOfFirst { it.id == replyMessageId }
                            if (targetIndex != -1) {
                                highlightedMessageId = replyMessageId
                                scope.launch {
                                    listState.animateScrollToItem(targetIndex)
                                    delay(2000)
                                    highlightedMessageId = 0L
                                }
                            }
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .navigationBarsPadding()
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Message") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                sendMessage(
                                    viewModel.currentChatId,
                                    inputText
                                )
                                inputText = ""
                                scope.launch { listState.animateScrollToItem(0) }
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(Icons.Default.Send, null)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: TdApi.Message,
    showSenderInfo: Boolean,
    viewModel: ChatViewModel,
    isHighlighted: Boolean,
    onReplyClick: (Long) -> Unit
) {
    val isMe = message.isOutgoing
    var senderName by remember { mutableStateOf("...") }
    var avatarPath by remember { mutableStateOf<String?>(null) }

    val highlightColor by animateColorAsState(
        targetValue = if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
        animationSpec = tween(durationMillis = 500)
    )

    LaunchedEffect(message.senderId) {
        if (!isMe && message.senderId is TdApi.MessageSenderUser) {
            val userId = (message.senderId as TdApi.MessageSenderUser).userId
            client?.send(TdApi.GetUser(userId)) { res ->
                if (res is TdApi.User) {
                    val photo = res.profilePhoto?.small
                    if (photo != null && !photo.local.isDownloadingCompleted) {
                        // ЕСЛИ НЕ СКАЧАНО — СКАЧИВАЕМ СРАЗУ
                        client?.send(TdApi.DownloadFile(photo.id, 1, 0, 0, true)) {}
                    } else {
                        avatarPath = photo?.local?.path
                    }
                }
            }
        }
    }

    LaunchedEffect(message.senderId) {
        if (!isMe) {
            viewModel.getUserName(message.senderId) { name -> senderName = name }
            if (message.senderId is TdApi.MessageSenderUser) {
                val userId = (message.senderId as TdApi.MessageSenderUser).userId
                client?.send(TdApi.GetUser(userId)) { res ->
                    if (res is TdApi.User) {
                        val photo = res.profilePhoto?.small
                        if (photo != null) {
                            if (photo.local.isDownloadingCompleted) {
                                avatarPath = photo.local.path
                            } else {
                                client?.send(TdApi.DownloadFile(photo.id, 1, 0, 0, true)) {}
                            }
                        }
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(highlightColor)
            .padding(horizontal = 8.dp, vertical = 1.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMe) {
            if (showSenderInfo) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer)) {
                    if (!avatarPath.isNullOrEmpty()) {
                        AsyncImage(model = avatarPath, contentDescription = null, contentScale = ContentScale.Crop)
                    } else {
                        Text(senderName.take(1).uppercase(), modifier = Modifier.align(Alignment.Center))
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(36.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            if (showSenderInfo && !isMe) {
                Text(
                    text = senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }

            Surface(
                color = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (!isMe && !showSenderInfo) 4.dp else 16.dp,
                    bottomEnd = if (isMe && !showSenderInfo) 4.dp else 16.dp
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    if (message.replyTo is TdApi.MessageReplyToMessage) {
                        ReplyPreview((message.replyTo as TdApi.MessageReplyToMessage).messageId, onReplyClick)
                    }
                    MessageContent(message.content)
                }
            }
        }
    }
}

@Composable
fun MessageContent(content: TdApi.MessageContent) {
    // Ограничиваем максимальную ширину медиа (например, 250dp)
    val mediaModifier = Modifier
        .widthIn(min=200.dp, max = 500.dp)
        .clip(RoundedCornerShape(8.dp))

    when (content) {
        is TdApi.MessageText -> {
            Text(text = content.text.toAnnotatedString(), style = MaterialTheme.typography.bodyLarge)
        }
        is TdApi.MessagePhoto -> {
            Column(modifier = Modifier.width(IntrinsicSize.Min)) {
                val photo = content.photo.sizes.lastOrNull()
                if (photo?.photo?.local?.isDownloadingCompleted == true) {
                    AsyncImage(
                        model = photo.photo.local.path,
                        contentDescription = null,
                        modifier = mediaModifier,
                        contentScale = ContentScale.FillWidth
                    )
                } else {
                    LaunchedEffect(photo) {
                        photo?.photo?.id?.let { client?.send(TdApi.DownloadFile(it, 1, 0, 0, true)) {} }
                    }
                    Box(modifier = mediaModifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                }
                // Добавляем подпись (caption), если она есть
                if (content.caption.text.isNotEmpty()) {
                    Text(
                        text = content.caption.toAnnotatedString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        is TdApi.MessageVideo -> {
            Column(modifier = Modifier.width(IntrinsicSize.Min)) {
                Box(contentAlignment = Alignment.Center, modifier = mediaModifier) {
                    val thumb = content.video.thumbnail?.file?.local?.path
                    AsyncImage(
                        model = thumb,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
                if (content.caption.text.isNotEmpty()) {
                    Text(
                        text = content.caption.toAnnotatedString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReplyPreview(replyId: Long, onClick: (Long) -> Unit) {
    Surface(
        modifier = Modifier
            .padding(bottom = 6.dp)
            .clickable { onClick(replyId) },
        color = Color.Black.copy(alpha = 0.08f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(modifier = Modifier.padding(4.dp).height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
            Spacer(Modifier.width(8.dp))
            Text("Ответ на сообщение", style = MaterialTheme.typography.labelSmall)
        }
    }
}