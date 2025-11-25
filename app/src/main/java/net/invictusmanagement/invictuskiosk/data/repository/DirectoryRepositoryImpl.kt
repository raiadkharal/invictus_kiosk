package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.commons.safeApiCall
import net.invictusmanagement.invictuskiosk.data.local.dao.DirectoryDao
import net.invictusmanagement.invictuskiosk.data.local.entities.toUnitList
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.toDigitalKey
import net.invictusmanagement.invictuskiosk.data.remote.dto.toUnitEntity
import net.invictusmanagement.invictuskiosk.data.remote.dto.toUnitList
import net.invictusmanagement.invictuskiosk.domain.model.DigitalKey
import net.invictusmanagement.invictuskiosk.domain.model.UnitList
import net.invictusmanagement.invictuskiosk.domain.repository.DirectoryRepository
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import javax.inject.Inject

class DirectoryRepositoryImpl @Inject constructor(
    private val api: ApiInterface,
    private val dao: DirectoryDao,
    private val logger: GlobalLogger
) : DirectoryRepository {

    private val logTag = "DirectoryRepository"

    override fun getUnitList(): Flow<Resource<List<UnitList>>> = flow {

        emit(Resource.Loading())

        emit(
            safeApiCall(
                logger = logger,
                tag = logTag,
                remoteCall = { api.getUnitList().map { it.toUnitList() } },
                localFallback = { dao.getUnits().map { it.toUnitList() } },
                errorMessage = "Failed to fetch unit list"
            )
        )
    }

//    override fun validateDigitalKey(digitalKeyDto: DigitalKeyDto): Flow<Resource<DigitalKey>> = flow {
//        try {
//            emit(Resource.Loading())
//            val response = api.validateDigitalKey(digitalKeyDto).toDigitalKey()
//            emit(Resource.Success(response))
//        } catch(e: Exception) {
//            emit(Resource.Error(mapError(e)))
//        }
//    }

    override fun validateDigitalKey(digitalKeyDto: DigitalKeyDto): Flow<Resource<DigitalKey>> = flow {

        emit(Resource.Loading())

        emit(
            safeApiCall(
                logger = logger,
                tag = "$logTag-validateDigitalKey",
                remoteCall = { api.validateDigitalKey(digitalKeyDto).toDigitalKey() },
                localFallback = null,   // NO fallback here
                errorMessage = "Failed to validate digital key: ${digitalKeyDto.key}"
            )
        )
    }

    override suspend fun sync() {
        safeApiCall(
            logger = logger,
            tag = "$logTag-sync-units",
            remoteCall = {
                val remoteUnits = api.getUnitList()
                dao.clearUnits()
                dao.insertUnits(remoteUnits.map { it.toUnitEntity() })
            },
            errorMessage = "Failed to sync units"
        )
    }

}