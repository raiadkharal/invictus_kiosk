package net.invictusmanagement.invictuskiosk.domain.repository

import kotlinx.coroutines.flow.Flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.domain.model.PromotionsCategory

interface CouponsRepository {
    fun getPromotionsCategories(): Flow<Resource<List<PromotionsCategory>>>
    fun getPromotionCodesById(id: String): Flow<Resource<List<String>>>
}