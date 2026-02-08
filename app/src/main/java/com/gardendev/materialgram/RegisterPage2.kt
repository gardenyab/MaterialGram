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
import com.gardendev.materialgram.TelegramClient

class RegisterPage2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page2)
        val button: Button = this.findViewById(R.id.regButton)
        val phone: EditText = this.findViewById(R.id.phoneEditText)

        button.setOnClickListener {
            if (phone.text.toString().trim() == "") {
                Toast.makeText(this, "Enter number", Toast.LENGTH_LONG).show()
            }
            else {
                val phoneN: String = phone.text.toString().trim()
                Log.d("MyLog", "Sending phone: $phoneN")
                TelegramClient.Telegram.client?.send(TdApi.SetAuthenticationPhoneNumber(phoneN, null)) { result ->
                    if (result is TdApi.Ok) {
                        startActivity(Intent(this, RegisterPage3::class.java))
                    }
                }
            }
        }
    }
}