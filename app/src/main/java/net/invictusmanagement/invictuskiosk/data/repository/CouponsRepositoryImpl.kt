package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.BusinessPromotionDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.toPromotionsCategory
import net.invictusmanagement.invictuskiosk.domain.model.PromotionsCategory
import net.invictusmanagement.invictuskiosk.domain.repository.CouponsRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CouponsRepositoryImpl @Inject constructor(
    private val api: ApiInterface
) : CouponsRepository {
    override fun getPromotionsCategories(): Flow<Resource<List<PromotionsCategory>>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getPromotionCategories().map { it.toPromotionsCategory() }
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    override fun getPromotionsByCategory(id: String): Flow<Resource<List<BusinessPromotionDto>>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getPromotionsByCategory(id)
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}