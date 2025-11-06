package net.invictusmanagement.invictuskiosk.data.remote.dto

data class RelayManagerLogDto(
    val logger: String,
    val exceptionMessage: String,
    val innerException: String,
    val kioskActivationCode: String
)