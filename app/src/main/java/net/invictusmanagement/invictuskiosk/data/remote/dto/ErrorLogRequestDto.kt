package net.invictusmanagement.invictuskiosk.data.remote.dto

data class ErrorLogRequestDto(
    val logger: String,
    val exceptionMessage: String,
    val innerException: String = "",
    val userId: Int = 0,
    val locationId: Int = 0,
    val role: String = ""
)
