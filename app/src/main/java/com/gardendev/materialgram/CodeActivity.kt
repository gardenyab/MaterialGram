package com.gardendev.materialgram

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.gardendev.materialgram.ui.theme.MaterialGramTheme
import org.drinkless.tdlib.TdApi

class CodeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialGramTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CodePage(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CodePage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var code by remember { mutableStateOf("") }
    var isCheckingCode by remember { mutableStateOf(false) }
    Box(
        modifier = modifier.fillMaxSize().padding(6.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            Icon(Icons.Default.Monitor, contentDescription = null, modifier=Modifier.size(100.dp, 100.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.SentAppCodeTitle),
                fontSize = TextUnit(18f, TextUnitType.Sp),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.SentAppCodeWithPhone),
                fontSize = TextUnit(15f, TextUnitType.Sp),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(50.dp))
            if (isCheckingCode) LinearProgressIndicator()
            TextField(
                value = code,
                onValueChange = { if (it.all { char -> char.isDigit() }) code = it },
                label = { Text(stringResource(R.string.SentSmsCodeTitle)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                leadingIcon = { Icon(Icons.Default.Sms, contentDescription = null) },
                shape = CircleShape
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = {
                    isCheckingCode = true
                    TelegramClient.Telegram.client?.send(TdApi.CheckAuthenticationCode(code)) { result ->
                        if (result is TdApi.Ok) {
                            TelegramClient.Telegram.client?.send(TdApi.GetAuthorizationState()) { state ->
                                when (state) {
                                    is TdApi.AuthorizationStateWaitPassword -> {
                                        val intent = Intent(context, TwoFAActivity::class.java)
                                        context.startActivity(intent)
                                    }

                                    is TdApi.AuthorizationStateReady -> {
                                        val intent = Intent(context, MainActivity::class.java)
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(intent)
                                    }
                                }
                            }
                            isCheckingCode = false
                        }
                        else {
                            isCheckingCode = false
                            code = ""
                            Toast.makeText(context, R.string.WrongCode, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                shape = ButtonDefaults.outlinedShape
            ) { Text(stringResource(R.string.Confirm))}
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE)
@Composable
fun GreetingPreview() {
    MaterialGramTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            CodePage(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}