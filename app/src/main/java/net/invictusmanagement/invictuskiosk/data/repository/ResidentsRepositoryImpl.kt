package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.commons.SafeApiCaller
import net.invictusmanagement.invictuskiosk.data.local.dao.DirectoryDao
import net.invictusmanagement.invictuskiosk.data.local.dao.ResidentsDao
import net.invictusmanagement.invictuskiosk.data.local.entities.toResident
import net.invictusmanagement.invictuskiosk.data.local.entities.toUnitList
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.toDigitalKey
import net.invictusmanagement.invictuskiosk.data.remote.dto.toResident
import net.invictusmanagement.invictuskiosk.domain.model.DigitalKey
import net.invictusmanagement.invictuskiosk.domain.model.Resident
import net.invictusmanagement.invictuskiosk.domain.repository.ResidentsRepository

class ResidentsRepositoryImpl(
    private val api: ApiInterface,
    private val residentsDao: ResidentsDao,
    private val directoryDao: DirectoryDao,
    private val safeApiCaller: SafeApiCaller
) : ResidentsRepository {

    private val logTag = "ResidentsRepository"

    override fun getResidentsByName(
        filter: String,
        byName: String
    ): Flow<Resource<List<Resident>>> = flow {
        emit(Resource.Loading())

        emit(
            safeApiCaller.call(
                tag = "$logTag-getResidentsByName",
                remoteCall = {
                    api.getResidentsByName(filter, byName).map { it.toResident() }
                },
                localFallback = {
                    residentsDao.getResidentsByName(filter, byName).map { it.toResident() }
                },
                errorMessage = "Failed to load residents by name"
            )
        )
    }

    override fun getResidentsByUnitNumber(unitNumber: String): Flow<Resource<List<Resident>>> =
        flow {
            emit(Resource.Loading())

            emit(
                safeApiCaller.call(
                    tag = "$logTag-getResidentsByUnitNumber",
                    remoteCall = {
                        api.getResidentsByUnitNumber(unitNumber).map { it.toResident() }
                    },
                    localFallback = {
                        val unitId = directoryDao.getUnitByNumber(unitNumber)?.toUnitList()?.id
                        residentsDao.getResidentsByUnit(unitId).map { it.toResident() }
                    },
                    errorMessage = "Failed to load residents for unit: $unitNumber"
                )
            )
        }

    override fun validateDigitalKey(digitalKeyDto: DigitalKeyDto): Flow<Resource<DigitalKey>> =
        flow {
            emit(Resource.Loading())

            emit(
                safeApiCaller.call(
                    tag = "$logTag-validateDigitalKey",
                    remoteCall = {
                        api.validateDigitalKey(digitalKeyDto).toDigitalKey()
                    },
                    errorMessage = "Failed to validate digital key"
                )
            )
        }

//    override fun validateDigitalKey(digitalKeyDto: DigitalKeyDto): Flow<Resource<DigitalKey>> =
//        flow {
//            try {
//                emit(Resource.Loading())
//                val response = api.validateDigitalKey(digitalKeyDto).toDigitalKey()
//                emit(Resource.Success(response))
//            } catch (e: HttpException) {
//                logger.logError(
//                    "validateDigitalKey",
//                    "Error validating digital key ${e.localizedMessage}",
//                    e
//                )
//                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
//            } catch (e: IOException) {
//                emit(Resource.Error("Couldn't reach server. Check your internet connection."))
//            }
//        }

    override fun getAllLeasingAgents(byName: String): Flow<Resource<List<Resident>>> = flow {

        emit(Resource.Loading())

        emit(
            safeApiCaller.call(
                tag = "$logTag-getAllLeasingAgents",
                remoteCall = {
                    api.getAllLeasingAgents(byName).map { it.toResident() }
                },
                localFallback = {
                    residentsDao.getAllLeasingAgents().map { it.toResident() }
                },
                errorMessage = "Failed to load leasing agents"
            )
        )
    }
}