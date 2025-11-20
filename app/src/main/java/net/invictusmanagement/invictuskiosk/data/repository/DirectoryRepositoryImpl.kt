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
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class DirectoryRepositoryImpl @Inject constructor(
    private val api: ApiInterface,
    private val dao: DirectoryDao
) : DirectoryRepository {

    override fun getUnitList(): Flow<Resource<List<UnitList>>> = flow {
        emit(Resource.Loading())

        try {
            fetchUnitList()
        } catch (e: Exception) {
            Log.d("getUnitList", mapError(e))
        }

        emitAll(
            dao.getUnits().map { list ->
                Resource.Success(list.map { it.toUnitList() })
            }
        )
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

    private suspend fun fetchUnitList() {
        val remote = api.getUnitList() // List<UnitListDto>
        dao.clearUnits()
        dao.insertUnits(remote.map { it.toUnitEntity() })
    }

    private fun mapError(e: Exception): String = when (e) {
        is IOException -> "Couldn't reach server. Check your internet connection."
        is HttpException -> e.localizedMessage ?: "Unexpected error"
        else -> "Something went wrong"
    }
}