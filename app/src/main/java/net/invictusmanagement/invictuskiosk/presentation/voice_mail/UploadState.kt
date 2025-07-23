package net.invictusmanagement.invictuskiosk.presentation.voice_mail

data class UploadState(
    val isLoading: Boolean = false,
    val data: Long = 0,
    val error: String = ""
)
