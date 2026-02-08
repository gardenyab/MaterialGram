package com.gardendev.materialgram

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.drinkless.tdlib.TdApi

class RegisterPage3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page3)
        val button: Button = this.findViewById(R.id.regButton)
        val codeE: EditText = this.findViewById(R.id.phoneEditText)

        button.setOnClickListener {
            if (codeE.text.toString().trim() == "") {
                Toast.makeText(this, "Enter number", Toast.LENGTH_LONG).show()
            }
            else {
                val code: String = codeE.text.toString().trim()
                Log.d("MyLog", "Sending phone: $code")
                TelegramClient.Telegram.client?.send(TdApi.CheckAuthenticationCode(code)) { result ->
                    runOnUiThread {
                        when (result) {
                            is TdApi.Ok -> {
                                // 1. ЕСЛИ ВСЁ ОК: аккаунт без 2FA, заходим в главное меню
                                TelegramClient.Telegram.client?.send(TdApi.GetAuthorizationState()) { state ->
                                    runOnUiThread {
                                        when (state) {
                                            is TdApi.AuthorizationStateWaitPassword -> {
                                                val intent = Intent(this, RegisterPage4::class.java)
                                                startActivity(intent)
                                            }

                                            is TdApi.AuthorizationStateReady -> {
                                                val intent = Intent(this, MainActivity::class.java)
                                                intent.flags =
                                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                startActivity(intent)
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
    }
}