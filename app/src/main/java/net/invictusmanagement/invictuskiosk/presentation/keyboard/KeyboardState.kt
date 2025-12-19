package net.invictusmanagement.invictuskiosk.presentation.keyboard

data class KeyboardState(
    val isVisible: Boolean = false,
    val text: String = "",
    val isPassword: Boolean = false,
    val keyboardType: KeyboardType = KeyboardType.QWERTY
)
