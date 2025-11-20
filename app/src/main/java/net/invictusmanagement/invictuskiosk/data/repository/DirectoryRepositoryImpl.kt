package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.toDigitalKey
import net.invictusmanagement.invictuskiosk.data.remote.dto.toResident
import net.invictusmanagement.invictuskiosk.data.remote.dto.toUnit
import net.invictusmanagement.invictuskiosk.data.remote.dto.toUnitList
import net.invictusmanagement.invictuskiosk.domain.model.DigitalKey
import net.invictusmanagement.invictuskiosk.domain.model.Resident
import net.invictusmanagement.invictuskiosk.domain.model.Unit
import net.invictusmanagement.invictuskiosk.domain.model.UnitList
import net.invictusmanagement.invictuskiosk.domain.repository.DirectoryRepository
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class DirectoryRepositoryImpl @Inject constructor(
    private val api: ApiInterface,
    private val logger: GlobalLogger
) : DirectoryRepository {

    override fun getUnitList(): Flow<Resource<List<UnitList>>> = flow{
        try {
            emit(Resource.Loading())
            val response = api.getUnitList().map { it.toUnitList() }
            emit(Resource.Success(response))
        } catch(e: HttpException) {
            logger.logError("getUnitList", "Error fetching unit list ${e.localizedMessage}",e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch(e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    override fun validateDigitalKey(digitalKeyDto: DigitalKeyDto): Flow<Resource<DigitalKey>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.validateDigitalKey(digitalKeyDto).toDigitalKey()
            emit(Resource.Success(response))
        } catch(e: HttpException) {
            logger.logError("validateDigitalKey", "Error validating digital key ${e.localizedMessage}",e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch(e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    override fun getAllResidents(): Flow<Resource<List<Resident>>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getAllResidents().map { it.toResident() }
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            logger.logError("getAllResidents", "Error fetching residents ${e.localizedMessage}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}