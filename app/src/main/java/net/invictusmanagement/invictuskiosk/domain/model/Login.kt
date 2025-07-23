package net.invictusmanagement.invictuskiosk.domain.model

data class Login(
    val success:Boolean,
    val token: String,
)