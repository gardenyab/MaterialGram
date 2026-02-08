package com.gardendev.materialgram

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.* // Это лечит mutableStateOf
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import com.gardendev.materialgram.TelegramClient


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            }
        }
    }
}