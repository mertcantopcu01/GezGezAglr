package com.example.myapplication.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

object TextFieldStyles {

    @Composable
    fun defaultTextFieldColors(
        // ———————————— Metin Renkleri ————————————
        focusedTextColor: Color = Color.Black,
        unfocusedTextColor: Color = Color.Black,
        disabledTextColor: Color = Color.Gray,
        errorTextColor: Color = Color.Red,

        // ————————— Container (Arka Plan) Renkleri —————————
        focusedContainerColor: Color = Color.White.copy(alpha = 0.5f),
        unfocusedContainerColor: Color = Color.White.copy(alpha = 0.5f),
        disabledContainerColor: Color = Color.White.copy(alpha = 0.5f),
        errorContainerColor: Color = Color.White.copy(alpha = 0.5f),

        // ————————— İmleç Renkleri —————————
        cursorColor: Color = MaterialTheme.colorScheme.primary,
        errorCursorColor: Color = Color.Red,

        // ————————— Kenarlık (Border) Renkleri —————————
        focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor: Color = Color.LightGray,
        disabledBorderColor: Color = Color.Gray,
        errorBorderColor: Color = Color.Red,

        // ————————— Icon / Label / Placeholder / vs. —————————
        focusedLeadingIconColor: Color = Color.Black,
        unfocusedLeadingIconColor: Color = Color.Gray,
        disabledLeadingIconColor: Color = Color.LightGray,
        errorLeadingIconColor: Color = Color.Red,

        focusedLabelColor: Color = MaterialTheme.colorScheme.primary,
        errorLabelColor: Color = Color.Red,

        disabledPlaceholderColor: Color = Color.LightGray,
        errorPlaceholderColor: Color = Color.Red,

        focusedSupportingTextColor: Color = Color.Gray,
        unfocusedSupportingTextColor: Color = Color.Gray,
        disabledSupportingTextColor: Color = Color.LightGray,
        errorSupportingTextColor: Color = Color.Red,

        focusedPrefixColor: Color = Color.Black,
        unfocusedPrefixColor: Color = Color.Gray,
        disabledPrefixColor: Color = Color.LightGray,
        errorPrefixColor: Color = Color.Red,

        focusedSuffixColor: Color = Color.Black,
        unfocusedSuffixColor: Color = Color.Gray,
        disabledSuffixColor: Color = Color.LightGray,
        errorSuffixColor: Color = Color.Red
    ): TextFieldColors {
        return OutlinedTextFieldDefaults.colors(
            // — Metin Renkleri —
            focusedTextColor = focusedTextColor,
            unfocusedTextColor = unfocusedTextColor,
            disabledTextColor = disabledTextColor,
            errorTextColor = errorTextColor,

            // — Container Renkleri —
            focusedContainerColor = focusedContainerColor,
            unfocusedContainerColor = unfocusedContainerColor,
            disabledContainerColor = disabledContainerColor,
            errorContainerColor = errorContainerColor,

            // — İmleç Renkleri —
            cursorColor = cursorColor,
            errorCursorColor = errorCursorColor,

            // — Kenarlık Renkleri —
            focusedBorderColor = focusedBorderColor,
            unfocusedBorderColor = unfocusedBorderColor,
            disabledBorderColor = disabledBorderColor,
            errorBorderColor = errorBorderColor,

            // — Icon Renkleri —
            focusedLeadingIconColor = focusedLeadingIconColor,
            unfocusedLeadingIconColor = unfocusedLeadingIconColor,
            disabledLeadingIconColor = disabledLeadingIconColor,
            errorLeadingIconColor = errorLeadingIconColor,

            // — Label Renkleri —
            focusedLabelColor = focusedLabelColor,
            errorLabelColor = errorLabelColor,

            // — Placeholder Renkleri —
            disabledPlaceholderColor = disabledPlaceholderColor,
            errorPlaceholderColor = errorPlaceholderColor,

            // — Supporting Text Renkleri —
            focusedSupportingTextColor = focusedSupportingTextColor,
            unfocusedSupportingTextColor = unfocusedSupportingTextColor,
            disabledSupportingTextColor = disabledSupportingTextColor,
            errorSupportingTextColor = errorSupportingTextColor,

            // — Prefix Renkleri —
            focusedPrefixColor = focusedPrefixColor,
            unfocusedPrefixColor = unfocusedPrefixColor,
            disabledPrefixColor = disabledPrefixColor,
            errorPrefixColor = errorPrefixColor,

            // — Suffix Renkleri —
            focusedSuffixColor = focusedSuffixColor,
            unfocusedSuffixColor = unfocusedSuffixColor,
            disabledSuffixColor = disabledSuffixColor,
            errorSuffixColor = errorSuffixColor
        )
    }
}
