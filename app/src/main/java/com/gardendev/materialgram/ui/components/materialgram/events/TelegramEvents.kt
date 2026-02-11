package com.gardendev.materialgram.ui.components.materialgram.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.drinkless.tdlib.TdApi

object TelegramEvents {
    private val _updates = MutableSharedFlow<TdApi.Object>(extraBufferCapacity = 64)
    val updates = _updates.asSharedFlow()

    fun emit(update: TdApi.Object) {
        _updates.tryEmit(update)
    }
}