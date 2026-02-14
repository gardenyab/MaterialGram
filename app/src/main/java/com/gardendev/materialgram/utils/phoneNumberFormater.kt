package com.gardendev.materialgram.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.google.i18n.phonenumbers.PhoneNumberUtil

class PhoneNumberFormatter : VisualTransformation {
    private val phoneUtil = PhoneNumberUtil.getInstance()

    override fun filter(text: AnnotatedString): TransformedText {
        val rawInput = text.text
        val fullNumber = if (rawInput.startsWith("+")) rawInput else "+$rawInput"
        val formatted = try {
            val numberProto = phoneUtil.parse(fullNumber, null)
            phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
        } catch (e: Exception) {
            fullNumber
        }

        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    return formatted.length
                }

                override fun transformedToOriginal(offset: Int): Int {
                    return text.length
                }
            }
        )
    }
}