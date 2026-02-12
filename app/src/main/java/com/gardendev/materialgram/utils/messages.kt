package com.gardendev.materialgram.utils

import com.gardendev.materialgram.TelegramClient.Telegram.client
import org.drinkless.tdlib.TdApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

fun sendMessage(
    chatId: Long,
    text: String,
    replyToMessageId: Long = 0
) {
    if (chatId == 0L || text.isBlank()) return

    val content = TdApi.InputMessageText(
        TdApi.FormattedText(text, null),
        null,
        true
    )

    val replyTo = if (replyToMessageId != 0L) {
        TdApi.InputMessageReplyToMessage(replyToMessageId, null, 0)
    } else null

    client?.send(TdApi.SendMessage(
        chatId,
        0,
        replyTo,
        null,
        null,
        content
    )) { }
}

fun TdApi.FormattedText.toAnnotatedString(): AnnotatedString {
    val builder = AnnotatedString.Builder(this.text)

    this.entities.forEach { entity ->
        val style = when (entity.type) {
            is TdApi.TextEntityTypeBold -> SpanStyle(fontWeight = FontWeight.Bold)
            is TdApi.TextEntityTypeItalic -> SpanStyle(fontStyle = FontStyle.Italic)
            is TdApi.TextEntityTypeCode -> SpanStyle(
                fontFamily = FontFamily.Monospace,
                background = Color.DarkGray.copy(alpha = 0.1f)
            )
            is TdApi.TextEntityTypeUnderline -> SpanStyle(textDecoration = TextDecoration.Underline)
            is TdApi.TextEntityTypeStrikethrough -> SpanStyle(textDecoration = TextDecoration.LineThrough)
            is TdApi.TextEntityTypeTextUrl -> SpanStyle(color = Color(0xFF2196F3), textDecoration = TextDecoration.Underline)
            else -> SpanStyle()
        }
        builder.addStyle(style, entity.offset, entity.offset + entity.length)
    }
    return builder.toAnnotatedString()
}