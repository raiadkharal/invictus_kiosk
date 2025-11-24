package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.local.dao.DirectoryDao
import net.invictusmanagement.invictuskiosk.data.local.dao.ResidentsDao
import net.invictusmanagement.invictuskiosk.data.local.entities.toResident
import net.invictusmanagement.invictuskiosk.data.local.entities.toUnitList
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.toDigitalKey
import net.invictusmanagement.invictuskiosk.data.remote.dto.toLogin
import net.invictusmanagement.invictuskiosk.data.remote.dto.toResident
import net.invictusmanagement.invictuskiosk.data.remote.dto.toUnit
import net.invictusmanagement.invictuskiosk.domain.model.DigitalKey
import net.invictusmanagement.invictuskiosk.domain.model.Resident
import net.invictusmanagement.invictuskiosk.domain.model.Unit
import net.invictusmanagement.invictuskiosk.domain.repository.ResidentsRepository
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import retrofit2.HttpException
import java.io.IOException

class ResidentsRepositoryImpl(
    private val api: ApiInterface,
    private val logger: GlobalLogger,
    private val residentsDao: ResidentsDao,
    private val directoryDao: DirectoryDao
) : ResidentsRepository {

    override fun getResidentsByName(
        filter: String,
        byName: String
    ): Flow<Resource<List<Resident>>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getResidentsByName(filter, byName).map { it.toResident() }
            emit(Resource.Success(response))
        } catch (e: Exception) {
            logger.logError(
                "getResidentsByName",
                "Error fetching residents by name ${e.localizedMessage}",
                e
            )
            val localData = residentsDao.getResidentsByName(filter, byName).map { it.toResident() }
            emit(
                Resource.Error(
                    data = localData,
                    message = e.localizedMessage ?: "An unexpected error occured"
                )
            )
        }
    }

    override fun getResidentsByUnitNumber(unitNumber: String): Flow<Resource<List<Resident>>> =
        flow {
            try {
                emit(Resource.Loading())
                val response = api.getResidentsByUnitNumber(unitNumber).map { it.toResident() }
                emit(Resource.Success(response))
            } catch (e: Exception) {
                logger.logError(
                    "getResidentsByUnitNumber",
                    "Error fetching residents by unit number ${e.localizedMessage}",
                    e
                )
                val unitId = directoryDao.getUnitByNumber(unitNumber)?.toUnitList()?.id
                val localData = residentsDao.getResidentsByUnit(unitId).map { it.toResident() }
                emit(
                    Resource.Error(
                        data = localData,
                        message = e.localizedMessage ?: "An unexpected error occured"
                    )
                )
            }
        }

    override fun validateDigitalKey(digitalKeyDto: DigitalKeyDto): Flow<Resource<DigitalKey>> =
        flow {
            try {
                emit(Resource.Loading())
                val response = api.validateDigitalKey(digitalKeyDto).toDigitalKey()
                emit(Resource.Success(response))
            } catch (e: HttpException) {
                logger.logError(
                    "validateDigitalKey",
                    "Error validating digital key ${e.localizedMessage}",
                    e
                )
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
            } catch (e: IOException) {
                emit(Resource.Error("Couldn't reach server. Check your internet connection."))
            }
        }

    override fun getAllLeasingAgents(byName: String): Flow<Resource<List<Resident>>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getAllLeasingAgents(byName).map { it.toResident() }
            emit(Resource.Success(response))
        } catch (e: Exception) {
            logger.logError(
                "getAllLeasingAgents",
                "Error fetching leasing agents by name ${e.localizedMessage}",
                e
            )
            val localData = residentsDao.getAllLeasingAgents().map { it.toResident() }
            emit(
                Resource.Error(
                    data = localData,
                    message = e.localizedMessage ?: "An unexpected error occured"
                )
            )
        }
    }
}