package com.gardendev.materialgram

import android.inputmethodservice.Keyboard
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gardendev.materialgram.TelegramClient.Telegram.client
import com.gardendev.materialgram.ui.theme.MaterialGramTheme
import com.gardendev.materialgram.utils.downloadFile
import com.gardendev.materialgram.utils.getUser
import com.gardendev.materialgram.utils.getUserFullInfo
import com.gardendev.materialgram.utils.toAnnotatedString
import org.drinkless.tdlib.TdApi
import kotlin.coroutines.resume

class UserInfoPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Получаем ID из интента
        val userId = intent.getLongExtra("USER_ID", 0L)

        setContent {
            // 2. Создаем состояние для пользователя
            var user by remember { mutableStateOf<TdApi.User?>(null) }

            // 3. Загружаем данные через LaunchedEffect (это и есть корутина)
            LaunchedEffect(userId) {
                if (userId != 0L) {
                    // Здесь вызываем вашу suspend функцию
                    user = getUser(userId)
                }
            }

            MaterialGramTheme {
                // 4. Проверяем, загрузился ли пользователь
                val currentUser = user
                if (currentUser != null) {
                    // Передаем уже не nullable объект
                    UserProfileScreen(
                        user = currentUser,
                        onBackClick = { finish() }
                    )
                } else {
                    // Пока данные грузятся, показываем индикатор
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    // Пример реализации функции внутри Activity (или вынесите в репозиторий)
    private suspend fun getUser(userId: Long): TdApi.User? {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            client?.send(TdApi.GetUser(userId)) { result ->
                if (result is TdApi.User) {
                    continuation.resume(result)
                } else {
                    continuation.resume(null)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE)
@Composable
fun UserProfilePreview() {
    // Создаем "фейкового" пользователя для отображения в студии
    val mockUser = TdApi.User().apply {
        firstName = "Garden"
        lastName = "Dev"
        phoneNumber = "1234567890"
        usernames = TdApi.Usernames(
            arrayOf("material_gram"), // activeUsernames
            arrayOf(""),                // disabledUsernames
            "",                       // editableUsername             // verificationStatus (или второй строковый параметр)
        )
    }

    MaterialGramTheme {
        UserProfileScreen(
            user = mockUser,
            onBackClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    user: TdApi.User,
    onBackClick: () -> Unit
) {
    var fullUser by remember {mutableStateOf<TdApi.UserFullInfo?>(null)}
    LaunchedEffect(Unit) {
        fullUser = getUserFullInfo(user.id)
        downloadFile(user.profilePhoto?.big?.id)
    }
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("${user.firstName} ${user.lastName.orEmpty()}") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Редактировать */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Аватарка ---
            val photoPath = user.profilePhoto?.big?.local?.path
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(top = 16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!photoPath.isNullOrEmpty()) {
                    AsyncImage(
                        model = photoPath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        modifier = Modifier.clip(CircleShape),
                        text = user.firstName.take(1),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Инфо-карточки ---
            InfoSection(
                title = "Account",
                items = listOf(
                    InfoItem(Icons.Default.Phone, "Phone", "+${user.phoneNumber.take(1)}***********"),
                    InfoItem(Icons.Default.AlternateEmail, "Username", user.usernames?.activeUsernames?.firstOrNull()?.let { "@$it" } ?: "None"),
                    InfoItem(Icons.Default.Info, "Bio", fullUser?.bio?.text.toString()) // Bio берется через GetUserFullInfo
                )
            )
        }
    }
}

@Composable
fun InfoSection(title: String, items: List<InfoItem>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = item.value, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

data class InfoItem(val icon: ImageVector, val label: String, val value: String)