package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.domain.repository.UnitMapRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UnitMapRepositoryImpl @Inject constructor(
    private val api: ApiInterface
) : UnitMapRepository {
    override fun getMapImage(
        unitId: Long,
        unitMapId: Long,
        toPackageCenter: Boolean
    ): Flow<Resource<ByteArray>> = flow {
        emit(Resource.Loading())

        try {
            val response = api.getMapImage(unitId, unitMapId, toPackageCenter)
            val bytes = response.bytes() // Convert response to ByteArray
            emit(Resource.Success(bytes))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

}