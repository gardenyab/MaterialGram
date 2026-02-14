package com.gardendev.materialgram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.gardendev.materialgram.TelegramClient.Telegram.client
import com.gardendev.materialgram.ui.theme.MaterialGramTheme
import com.gardendev.materialgram.utils.downloadFile
import com.gardendev.materialgram.utils.formatDate
import com.gardendev.materialgram.utils.getUserFullInfo
import org.drinkless.tdlib.TdApi
import kotlin.coroutines.resume
import com.gardendev.materialgram.utils.getUserPic

class UserInfoPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = intent.getLongExtra("USER_ID", 0L)

        setContent {
            var user by remember { mutableStateOf<TdApi.User?>(null) }

            LaunchedEffect(userId) {
                if (userId != 0L) {
                    user = getUser(userId)
                }
            }

            MaterialGramTheme {
                val currentUser = user
                if (currentUser != null) {
                    UserProfileScreen(
                        user = currentUser,
                        onBackClick = { finish() }
                    )
                } else {
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
    val mockUser = TdApi.User().apply {
        firstName = "Garden"
        lastName = "Dev"
        phoneNumber = "1234567890"
        usernames = TdApi.Usernames(
            arrayOf("material_gram"),
            arrayOf(""),
            "",
        )
        status = TdApi.UserStatusOnline()
    }

    val fullUser = TdApi.UserFullInfo().apply {
        firstProfileAudio = TdApi.Audio(
            190,
            "TestAudio",
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
        bio = TdApi.FormattedText("Test bio", null)
        birthdate = TdApi.Birthdate(
            31,
            12,
            2026
        )
        businessInfo = TdApi.BusinessInfo(
            TdApi.BusinessLocation(null, "Test adress"),
            null,
            null,
            100,
            1500,
            null,
            null,
            null)
        rating = TdApi.UserRating(
            1,
            false,
            1234,
            500,
            5000
        )
    }

    MaterialGramTheme {
        UserProfileScreen(
            user = mockUser,
            fullUser = fullUser,
            onBackClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    user: TdApi.User,
    fullUser: TdApi.UserFullInfo? = null,
    onBackClick: () -> Unit
) {
    var fullUser by remember {mutableStateOf<TdApi.UserFullInfo?>(fullUser)}
    LaunchedEffect(Unit) {
        if (fullUser == null)
            fullUser = getUserFullInfo(user.id)
        downloadFile(user.profilePhoto?.big?.id)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    /*IconButton(onClick = { /* Редактировать */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }*/
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(end = 0.dp)
            ) {
                getUserPic(user)
                Spacer(modifier = Modifier.width(30.dp))
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "${user.firstName} ${user.lastName.orEmpty()}",
                        fontSize = TextUnit(25f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold
                    )
                    var status: String? = null

                    if (user.status is TdApi.UserStatusOffline) status = "Was ${formatDate((user.status as TdApi.UserStatusOffline).wasOnline)}"
                    else if (user.status is TdApi.UserStatusOnline) status = "Online"
                    else if (user.status is TdApi.UserStatusRecently) status = "Was recently"
                    else if (user.status is TdApi.UserStatusLastMonth) status = "Was at this month"
                    else if (user.status is TdApi.UserStatusLastWeek) status = "Was at this week"
                    else if (user.status is TdApi.UserStatusEmpty) status = "Offline"
                    else status = "Idk"

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        "${status}",
                        fontStyle = FontStyle.Italic
                    )
                    if (fullUser?.firstProfileAudio != null) {
                        Text(
                            "${fullUser?.firstProfileAudio?.title ?: "None"}",
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            var items: List<InfoItem> = listOf()

            if (user.phoneNumber != "") items += InfoItem(Icons.Default.Phone, "Phone", "+${user.phoneNumber.take(1)}***********")
            if (user.usernames?.activeUsernames?.firstOrNull() != null) items += InfoItem(Icons.Default.AlternateEmail, "Username", user.usernames?.activeUsernames?.firstOrNull()?.let { "@$it" } ?: "None")
            if (fullUser?.bio?.text != null) items += InfoItem(Icons.Default.Info, "Bio", fullUser?.bio?.text.toString())
            if (fullUser?.birthdate != null) items += InfoItem(Icons.Default.Cake, "Birthday", "${fullUser?.birthdate?.day}.${fullUser?.birthdate?.month}.${fullUser?.birthdate?.year}")
            if (fullUser?.businessInfo?.location != null) items += InfoItem(Icons.Default.Map, "Adress", "${fullUser?.businessInfo?.location?.address}")
            if (fullUser?.rating != null) items += InfoItem(Icons.Default.StarRate, "Rating", "${fullUser?.rating?.level}")
            InfoSection(
                title = "Account",
                items = items
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