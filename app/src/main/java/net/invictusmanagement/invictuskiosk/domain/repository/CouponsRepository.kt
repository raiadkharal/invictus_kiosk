package net.invictusmanagement.invictuskiosk.domain.repository

import kotlinx.coroutines.flow.Flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.dto.BusinessPromotionDto
import net.invictusmanagement.invictuskiosk.domain.model.PromotionsCategory

interface CouponsRepository {
    fun getPromotionsCategories(): Flow<Resource<List<PromotionsCategory>>>
    fun getPromotionsByCategory(id: String): Flow<Resource<List<BusinessPromotionDto>>>
}