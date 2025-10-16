package net.invictusmanagement.invictuskiosk.data.remote.dto

data class BusinessPromotionDto(
    val address1: String?,
    val address2: Any?,
    val city: String?,
    val name: String?,
    val phone: String?,
    val promotions: List<PromotionDto>?,
    val state: String?,
    val zip: String?
)