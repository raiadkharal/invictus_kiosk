package net.invictusmanagement.invictuskiosk.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.local.dao.CouponsDao
import net.invictusmanagement.invictuskiosk.data.local.entities.toBusinessPromotion
import net.invictusmanagement.invictuskiosk.data.local.entities.toPromotionsCategory
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.toBusinessPromotion
import net.invictusmanagement.invictuskiosk.data.remote.dto.toEntity
import net.invictusmanagement.invictuskiosk.domain.model.BusinessPromotion
import net.invictusmanagement.invictuskiosk.domain.model.PromotionsCategory
import net.invictusmanagement.invictuskiosk.domain.repository.CouponsRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CouponsRepositoryImpl @Inject constructor(
    private val api: ApiInterface,
    private val couponsDao: CouponsDao
) : CouponsRepository {

    override fun getCouponsCategories(): Flow<Resource<List<PromotionsCategory>>> = flow {
        emit(Resource.Loading())

        try {
            fetchCouponsCategories()
        } catch (e: Exception) {
            Log.d("getPromotionsCategories", mapError(e))
        }

        emitAll(
            couponsDao.getPromotionCategories().map { list ->
                Resource.Success(list.map { it.toPromotionsCategory() })
            }
        )
    }

    override fun getCouponsByCategory(
        id: String
    ): Flow<Resource<List<BusinessPromotion>>> = flow {

        emit(Resource.Loading())

        try {
            fetchCouponsByCategory(id)   // ⬅ fetch API and update DB inside this function
        } catch (e: Exception) {
            Log.d("getCouponsByCategory", mapError(e))
        }

        emitAll(
            couponsDao.getPromotions().map { list ->
                Resource.Success(list.map { it.toBusinessPromotion() })
            }
        )
    }


    private suspend fun fetchCouponsCategories() {
        val remote = api.getPromotionCategories()
        couponsDao.clearPromotionCategories()
        couponsDao.insertPromotionCategories(remote.map { it.toEntity() })
    }

    private suspend fun fetchCouponsByCategory(id: String) {
        val remote = api.getPromotionsByCategory(id)

        couponsDao.insertPromotions(
            remote.map { it.toEntity() }
        )
    }

    private fun mapError(e: Exception): String = when (e) {
        is IOException -> "Couldn't reach server. Check your internet connection."
        is HttpException -> e.localizedMessage ?: "Unexpected error"
        else -> "Something went wrong"
    }


    override suspend fun syncAllCoupons() {
        // --- Sync categories ---
        runCatching {
            val categoriesRemote = api.getPromotionCategories()
            couponsDao.clearPromotionCategories()
            couponsDao.insertPromotionCategories(categoriesRemote.map { it.toEntity() })
        }.onFailure { e ->
            Log.w("CouponsRepository", "Failed to fetch categories: ${e.message}")
        }

        // --- Sync promotions ---
        runCatching {
            val promotions = api.getAllPromotions()
            couponsDao.insertPromotions(promotions.map { it.toEntity() })
        }.onFailure { e ->
            Log.w("CouponsRepository", "Failed to fetch promotions: ${e.message}")
        }
    }
}