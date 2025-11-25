package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.commons.safeApiCall
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
    private val dao: CouponsDao,
    private val logger: GlobalLogger
) : CouponsRepository {

    private val logTag = "CouponsRepository"

    override fun getCouponsCategories(): Flow<Resource<List<PromotionsCategory>>> = flow {
        emit(Resource.Loading())

        emit(
        safeApiCall(
            logger = logger,
            tag = "$logTag-getCouponsCategories",
            remoteCall = { api.getPromotionCategories().map { it.toPromotionsCategory() } },
            localFallback = { dao.getPromotionCategories().map { it.toPromotionsCategory() } },
            errorMessage = "Failed to load coupon categories"
        )
        )
    }

    override fun getCouponsByCategory(id: String): Flow<Resource<List<BusinessPromotion>>> = flow {
        emit(Resource.Loading())

        emit(
        safeApiCall(
            logger = logger,
            tag = "$logTag-getCouponsByCategory",
            remoteCall = { api.getPromotionsByCategory(id).map { it.toBusinessPromotion() } },
            localFallback = { dao.getPromotionsByCategory(id.toInt()).map { it.toBusinessPromotion() } },
            errorMessage = "Failed to load coupons for category: $id"
        )
        )
    }

    override suspend fun sync() {

        safeApiCall(
            logger = logger,
            tag = "$logTag-sync-categories",
            remoteCall = {
                val remote = api.getPromotionCategories()
                dao.clearPromotionCategories()
                dao.insertPromotionCategories(remote.map { it.toEntity() })
            },
            errorMessage = "Failed to sync categories"
        )

        safeApiCall(
            logger = logger,
            tag = "$logTag-sync-promotions",
            remoteCall = {
                val remote = api.getAllPromotions()
                dao.clearPromotions()
                dao.insertPromotions(remote.map { it.toEntity() })
            },
            errorMessage = "Failed to sync promotions"
        )
    }

}
