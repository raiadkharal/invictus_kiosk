package net.invictusmanagement.invictuskiosk.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BusinessPromotion(
    val type: Int,
    val address1: String,
    val address2: String,
    val city: String,
    val name: String,
    val phone: String,
    val promotions: List<Promotion>,
    val state: String,
    val zip: String
)