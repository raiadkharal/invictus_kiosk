package net.invictusmanagement.invictuskiosk.presentation.coupons

import net.invictusmanagement.invictuskiosk.domain.model.PromotionsCategory

data class CouponsCategoryState(
    val isLoading: Boolean = false,
    val couponsCategories: List<PromotionsCategory> = emptyList(),
    val error: String = ""
)