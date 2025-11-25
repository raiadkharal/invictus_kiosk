package net.invictusmanagement.invictuskiosk.domain.repository

import kotlinx.coroutines.flow.Flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.repository.Repository
import net.invictusmanagement.invictuskiosk.domain.model.BusinessPromotion
import net.invictusmanagement.invictuskiosk.domain.model.PromotionsCategory

interface CouponsRepository: Repository {
    fun getCouponsCategories(): Flow<Resource<List<PromotionsCategory>>>
    fun getCouponsByCategory(id: String): Flow<Resource<List<BusinessPromotion>>>
}