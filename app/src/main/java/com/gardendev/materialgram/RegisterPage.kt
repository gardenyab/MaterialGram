package com.gardendev.materialgram

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page)
        val button: Button = this.findViewById(R.id.regButton)

        button.setOnClickListener {
            val intent = Intent(this, RegisterPage2::class.java)
            startActivity(intent)
            finish()
        }
    }
}