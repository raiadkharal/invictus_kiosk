package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.ServiceKeyDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.toDigitalKey
import net.invictusmanagement.invictuskiosk.data.remote.dto.toServiceKey
import net.invictusmanagement.invictuskiosk.domain.model.DigitalKey
import net.invictusmanagement.invictuskiosk.domain.model.ServiceKey
import net.invictusmanagement.invictuskiosk.domain.repository.ServiceKeyRepository
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ServiceKeyRepositoryImpl @Inject constructor(
    private val api: ApiInterface,
    private val logger: GlobalLogger
): ServiceKeyRepository {

    override fun validateServiceKey(serviceKeyDto: ServiceKeyDto): Flow<Resource<ServiceKey>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.validateServiceKey(serviceKeyDto).toServiceKey()
            emit(Resource.Success(response))
        } catch(e: HttpException) {
            logger.logError("validateServiceKey", "Error validating service key ${e.localizedMessage}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch(e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}