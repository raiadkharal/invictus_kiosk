package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.commons.safeApiCall
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.domain.repository.UnitMapRepository
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlin.math.log

class UnitMapRepositoryImpl @Inject constructor(
    private val api: ApiInterface,
    private val logger: GlobalLogger
) : UnitMapRepository {
    private val logTag = "UnitMapRepository"

    override fun getMapImage(
        unitId: Long,
        unitMapId: Long,
        toPackageCenter: Boolean
    ): Flow<Resource<ByteArray>> = flow {
        emit(Resource.Loading())

        val result = safeApiCall(
            logger = logger,
            tag = "$logTag-getMapImage",
            remoteCall = {
                api.getMapImage(unitId, unitMapId, toPackageCenter).bytes()
            },
            localFallback = null,
            errorMessage = "Failed to load map image"
        )

        emit(result)
    }

    override fun getUnitImage(
        unitId: Long,
        unitImageId: Long
    ): Flow<Resource<ByteArray>> = flow {
        emit(Resource.Loading())

        emit(
            safeApiCall(
                logger = logger,
                tag = "$logTag-getUnitImage",
                remoteCall = {
                    api.getUnitImage(unitId, unitImageId).bytes()
                },
                localFallback = null,
                errorMessage = "Failed to load unit image"
            )
        )
    }

}