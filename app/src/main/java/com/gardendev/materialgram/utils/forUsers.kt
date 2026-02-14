package com.gardendev.materialgram.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gardendev.materialgram.TelegramClient.Telegram.client
import org.drinkless.tdlib.TdApi

suspend fun getUserFullInfo(userId: Long): TdApi.UserFullInfo? {
    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        client?.send(TdApi.GetUserFullInfo(userId)) { result ->
            if (result is TdApi.UserFullInfo) continuation.resume(result, null)
            else continuation.resume(null, null)
        }
    }
}

suspend fun getUser(userId: Long): TdApi.User? {
    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        client?.send(TdApi.GetUserFullInfo(userId)) { result ->
            if (result is TdApi.User) continuation.resume(result, null)
            else continuation.resume(null, null)
        }
    }
}

suspend fun getMe(): TdApi.User {
    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        client?.send(TdApi.GetMe()) { result ->
            if (result is TdApi.User) continuation.resume(result, null)
        }
    }
}

@Composable
fun getUserPic(user: TdApi.User) {
    val photoPath = user.profilePhoto?.big?.local?.path
    LaunchedEffect(Unit) {
        downloadFile(user.profilePhoto?.big?.id)
    }
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (!photoPath.isNullOrEmpty()) {
            AsyncImage(
                model = photoPath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                modifier = Modifier.clip(CircleShape),
                text = user.firstName.take(1),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun getChatPic(chat: TdApi.Chat?, size: Dp = 120.dp) {
    val photo = chat?.photo?.small

    LaunchedEffect(Unit) {
        downloadFile(photo?.id)
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (!photo?.local?.path.isNullOrEmpty()) {
            AsyncImage(
                model = photo?.local?.path,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                modifier = Modifier.clip(CircleShape),
                text = "${chat?.title?.take(1)}",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}