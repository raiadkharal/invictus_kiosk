package net.invictusmanagement.invictuskiosk.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.local.dao.DirectoryDao
import net.invictusmanagement.invictuskiosk.data.local.entities.toUnitList
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.toDigitalKey
import net.invictusmanagement.invictuskiosk.data.remote.dto.toUnitList
import net.invictusmanagement.invictuskiosk.data.remote.dto.toUnitEntity
import net.invictusmanagement.invictuskiosk.domain.model.DigitalKey
import net.invictusmanagement.invictuskiosk.domain.model.UnitList
import net.invictusmanagement.invictuskiosk.domain.repository.DirectoryRepository
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class DirectoryRepositoryImpl @Inject constructor(
    private val api: ApiInterface,
    private val dao: DirectoryDao,
    private val logger: GlobalLogger
) : DirectoryRepository {

    override fun getUnitList(): Flow<Resource<List<UnitList>>> = flow {

        try {
            emit(Resource.Loading())
            val response = api.getUnitList().map { it.toUnitList() }
            emit(Resource.Success(response))
        } catch (e: Exception) {
            logger.logError(
                "getUnitList",
                "Error fetching unit list: ${e.localizedMessage}",
                e
            )
            val localData = dao.getUnits().map { it.toUnitList() }
            emit(Resource.Error(data = localData, message = e.localizedMessage ?: "An unexpected error occurred"))
        }
    }


    override fun validateDigitalKey(digitalKeyDto: DigitalKeyDto): Flow<Resource<DigitalKey>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.validateDigitalKey(digitalKeyDto).toDigitalKey()
            emit(Resource.Success(response))
        } catch(e: Exception) {
            emit(Resource.Error(mapError(e)))
        }
    }

    private fun mapError(e: Exception): String = when (e) {
        is IOException -> "Couldn't reach server. Check your internet connection."
        is HttpException -> e.localizedMessage ?: "Unexpected error"
        else -> "Something went wrong"
    }

    override suspend fun sync() {
        runCatching {
            val remoteUnits = api.getUnitList()
            dao.clearUnits()
            dao.insertUnits(remoteUnits.map { it.toUnitEntity() })
        }.onFailure { e ->
            logger.logError("syncAllUnits", "Failed to sync units: ${e.localizedMessage}", e)
        }
    }
}