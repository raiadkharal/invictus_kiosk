package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.domain.model.Login

data class LoginDto(
    val success: Boolean = false,
    val activationCode: String?,
    val token: String? = "",
    val timeZoneOffset: Int? = 0
)


fun LoginDto.toLogin(): Login {
    return Login(
        success = success,
        token = token ?: ""
    )
}