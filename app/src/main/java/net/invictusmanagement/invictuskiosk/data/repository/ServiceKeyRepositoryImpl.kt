package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.commons.safeApiCall
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
) : ServiceKeyRepository {

    private val logTag = "ServiceKeyRepository"

    override fun validateServiceKey(serviceKeyDto: ServiceKeyDto): Flow<Resource<ServiceKey>> =
        flow {
            emit(Resource.Loading())

            emit(
                safeApiCall(
                    logger = logger,
                    tag = "$logTag-validateServiceKey",
                    remoteCall = {
                        api.validateServiceKey(serviceKeyDto).toServiceKey()
                    },
                    // no local fallback for this request
                    localFallback = null,
                    errorMessage = "Failed to validate service key"
                )
            )
        }
}