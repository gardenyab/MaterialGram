package com.gardendev.materialgram.utils

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