package net.invictusmanagement.invictuskiosk.presentation.keyboard

import androidx.compose.ui.text.input.TextFieldValue

data class KeyboardState(
    val isVisible: Boolean = false,
    val value: TextFieldValue = TextFieldValue(""),
    val isPassword: Boolean = false,
    val keyboardType: KeyboardType = KeyboardType.QWERTY
)
