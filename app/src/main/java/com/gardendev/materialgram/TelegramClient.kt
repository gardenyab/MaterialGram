package com.gardendev.materialgram

import org.drinkless.tdlib.Client

class TelegramClient {
    object Telegram {
        var client: Client? = null

        fun initClient(handler: Client.ResultHandler) {
            if (client == null) {
                client = Client.create(handler, null, null)
            }
        }
    }
}