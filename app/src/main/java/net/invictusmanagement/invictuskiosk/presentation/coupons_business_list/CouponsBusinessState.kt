package net.invictusmanagement.invictuskiosk.presentation.coupons_business_list

import net.invictusmanagement.invictuskiosk.domain.model.BusinessPromotion

data class CouponsBusinessState(
    val isLoading: Boolean = false,
    val businessPromotions: List<BusinessPromotion> = emptyList(),
    val error: String = ""
)