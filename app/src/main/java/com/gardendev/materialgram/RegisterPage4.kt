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

class RegisterPage4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page4)
        val button: Button = this.findViewById(R.id.regButton)
        val passwordE: EditText = this.findViewById(R.id.phoneEditText)

        button.setOnClickListener {
            if (passwordE.text.toString().trim() == "") {
                Toast.makeText(this, "Enter password", Toast.LENGTH_LONG).show()
            }
            else {
                val password: String = passwordE.text.toString().trim()
                Log.d("MyLog", "Sending phone: $password")
                TelegramClient.Telegram.client?.send(TdApi.CheckAuthenticationPassword(password)) { result ->
                    runOnUiThread {
                        when (result) {
                            is TdApi.Ok -> {
                                // Победа! Полный вход выполнен
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            is TdApi.Error -> {
                                Toast.makeText(this, "Неверный пароль: ${result.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}