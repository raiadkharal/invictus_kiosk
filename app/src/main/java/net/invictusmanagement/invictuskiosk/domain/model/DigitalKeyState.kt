package net.invictusmanagement.invictuskiosk.domain.model

data class DigitalKeyState(
    val isLoading: Boolean = false,
    val digitalKey: DigitalKey? = null,
    val error: String = ""
)