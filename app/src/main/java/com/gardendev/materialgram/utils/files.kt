package com.gardendev.materialgram.utils

import com.gardendev.materialgram.TelegramClient.Telegram.client
import org.drinkless.tdlib.TdApi

suspend fun downloadFile(fileId: Int?): Boolean {
    if (fileId == null) return false
    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        client?.send(TdApi.DownloadFile(fileId, 1, 0, 0, true)) { result ->
            if (result is TdApi.Ok) true
            else false
        }
    }
}