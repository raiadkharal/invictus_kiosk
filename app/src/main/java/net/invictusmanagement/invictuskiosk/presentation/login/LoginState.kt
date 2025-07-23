package net.invictusmanagement.invictuskiosk.presentation.login

import net.invictusmanagement.invictuskiosk.domain.model.Login

data class LoginState(
    val isLoading: Boolean = false,
    val login: Login? = null,
    val error: String = ""
)
