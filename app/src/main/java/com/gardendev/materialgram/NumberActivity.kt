package com.gardendev.materialgram

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.gardendev.materialgram.ui.components.IndeterminateCircularWavyProgressIndicator
import com.gardendev.materialgram.ui.theme.MaterialGramTheme
import com.gardendev.materialgram.utils.PhoneNumberFormatter
import com.google.android.material.color.MaterialColors
import org.drinkless.tdlib.TdApi

class NumberActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialGramTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NumberPage(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun NumberPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    val isError = phoneNumber.length > 0 && phoneNumber.length < 10
    var isSendingCode by remember { mutableStateOf(false) }
    val phoneTransformation = remember { PhoneNumberFormatter() }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            Icon(Icons.Default.SimCard, contentDescription = null, modifier=Modifier.size(100.dp, 100.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your Phone",
                fontSize = TextUnit(30f, TextUnitType.Sp),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Please, enter your phone number",
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
                label = { Text(stringResource(R.string.PhoneNumber)) },
                supportingText = {
                    if (isError) {
                        Text(stringResource(R.string.InvalidPhoneNumber), color = MaterialTheme.colorScheme.error)
                    }
                },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                visualTransformation = phoneTransformation,
                shape = CircleShape
            )
            Button(
                onClick = {
                    isSendingCode = true
                    TelegramClient.Telegram.client?.send(TdApi.SetAuthenticationPhoneNumber(phoneNumber, null)) { result ->
                        if (result is TdApi.Ok) {
                            context.startActivity(Intent(context, CodeActivity::class.java))
                            isSendingCode = false
                        }
                        else {
                            isSendingCode = false
                            phoneNumber = ""
                            Toast.makeText(context, R.string.InvalidPhoneNumber, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                shape = ButtonDefaults.outlinedShape
            ) { Text(stringResource(R.string.Next))}
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE)
@Composable
fun NumberPagePreview() {
    MaterialGramTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NumberPage(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}