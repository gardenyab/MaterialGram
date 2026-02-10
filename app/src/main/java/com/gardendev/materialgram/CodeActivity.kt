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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
    var phoneNumber by remember { mutableStateOf("") }
    val isError = phoneNumber.length > 5
    var isSendingCode by remember { mutableStateOf(false) }
    Box(
        modifier = modifier.fillMaxSize().padding(6.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                fontSize = TextUnit(40f, TextUnitType.Sp),
                fontWeight = FontWeight.Bold
            )
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
            if (isSendingCode) LinearProgressIndicator()
            TextField(
                value = phoneNumber,
                onValueChange = { if (it.all { char -> char.isDigit() }) phoneNumber = it },
                isError = isError,
                label = { Text(stringResource(R.string.SentSmsCodeTitle)) },
                supportingText = {
                    if (isError) {
                        Text(stringResource(R.string.WrongCode), color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Button(
                onClick = {
                    isSendingCode = true
                    TelegramClient.Telegram.client?.send(TdApi.SetAuthenticationPhoneNumber(phoneNumber, null)) { result ->
                        if (result is TdApi.Ok) {
                            context.startActivity(Intent(context, RegisterPage3::class.java))
                            isSendingCode = false
                        }
                        else {
                            isSendingCode = false
                            phoneNumber = ""
                            Toast.makeText(context, R.string.WrongCode, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                shape = ButtonDefaults.elevatedShape
            ) { Text(stringResource(R.string.Next))}
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE)
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