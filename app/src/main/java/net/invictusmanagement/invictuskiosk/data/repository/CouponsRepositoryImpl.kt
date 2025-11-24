package net.invictusmanagement.invictuskiosk.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.local.dao.CouponsDao
import net.invictusmanagement.invictuskiosk.data.local.entities.toBusinessPromotion
import net.invictusmanagement.invictuskiosk.data.local.entities.toPromotionsCategory
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.toBusinessPromotion
import net.invictusmanagement.invictuskiosk.data.remote.dto.toEntity
import net.invictusmanagement.invictuskiosk.data.remote.dto.toPromotionsCategory
import net.invictusmanagement.invictuskiosk.domain.model.BusinessPromotion
import net.invictusmanagement.invictuskiosk.domain.model.PromotionsCategory
import net.invictusmanagement.invictuskiosk.domain.repository.CouponsRepository
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import javax.inject.Inject

class CouponsRepositoryImpl @Inject constructor(
    private val api: ApiInterface,
    private val couponsDao: CouponsDao,
    private val logger: GlobalLogger
) : CouponsRepository {

    override fun getCouponsCategories(): Flow<Resource<List<PromotionsCategory>>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getPromotionCategories().map { it.toPromotionsCategory() }
            emit(Resource.Success(response))
        } catch (e: Exception) {
            logger.logError(
                "validateServiceKey",
                "Error validating service key ${e.localizedMessage}",
                e
            )
            val localData = couponsDao.getPromotionCategories().map { it.toPromotionsCategory() }
            emit(Resource.Error(data = localData, message =  e.localizedMessage ?: "An unexpected error occured"))
        }
    }

    override fun getCouponsByCategory(
        id: String
    ): Flow<Resource<List<BusinessPromotion>>> = flow {

        try {
            emit(Resource.Loading())
            val response = api.getPromotionsByCategory(id).map { it.toBusinessPromotion() }
            emit(Resource.Success(response))
        } catch (e: Exception) {
            logger.logError(
                "validateServiceKey",
                "Error validating service key ${e.localizedMessage}",
                e
            )
            val localData =
                couponsDao.getPromotionsByCategory(id.toInt()).map { it.toBusinessPromotion() }
            emit(Resource.Error(data = localData, message = e.localizedMessage ?: "An unexpected error occured"))
        }
    }

    override suspend fun sync() {
        // --- Sync categories ---
        runCatching {
            val categoriesRemote = api.getPromotionCategories()
            couponsDao.clearPromotionCategories()
            couponsDao.insertPromotionCategories(categoriesRemote.map { it.toEntity() })
        }.onFailure { e ->
            logger.logError("syncAllCoupons", "Failed to sync categories: ${e.localizedMessage}", e)
            Log.w("CouponsRepository", "Failed to fetch categories: ${e.message}")
        }

        // --- Sync promotions ---
        runCatching {
            val promotions = api.getAllPromotions()
            couponsDao.clearPromotions()
            couponsDao.insertPromotions(promotions.map { it.toEntity() })
        }.onFailure { e ->
            logger.logError("syncAllCoupons", "Failed to sync promotions: ${e.localizedMessage}", e)
        }
    }
}