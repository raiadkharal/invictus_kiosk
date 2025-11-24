package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.local.dao.HomeDao
import net.invictusmanagement.invictuskiosk.data.local.entities.IntroButtonEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.toAccessPoint
import net.invictusmanagement.invictuskiosk.data.local.entities.toLeasingOffice
import net.invictusmanagement.invictuskiosk.data.local.entities.toMain
import net.invictusmanagement.invictuskiosk.data.local.entities.toResident
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.home.toEntity
import net.invictusmanagement.invictuskiosk.data.remote.dto.home.toMain
import net.invictusmanagement.invictuskiosk.data.remote.dto.toAccessPoint
import net.invictusmanagement.invictuskiosk.data.remote.dto.toDigitalKey
import net.invictusmanagement.invictuskiosk.data.remote.dto.toEntity
import net.invictusmanagement.invictuskiosk.data.remote.dto.toLeasingOffice
import net.invictusmanagement.invictuskiosk.data.remote.dto.toResident
import net.invictusmanagement.invictuskiosk.data.remote.dto.toResidentEntity
import net.invictusmanagement.invictuskiosk.domain.model.AccessPoint
import net.invictusmanagement.invictuskiosk.domain.model.DigitalKey
import net.invictusmanagement.invictuskiosk.domain.model.LeasingOffice
import net.invictusmanagement.invictuskiosk.domain.model.Resident
import net.invictusmanagement.invictuskiosk.domain.model.home.Main
import net.invictusmanagement.invictuskiosk.domain.repository.HomeRepository
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val api: ApiInterface,
    private val homeDao: HomeDao,
    private val logger: GlobalLogger
) : HomeRepository {

    override fun validateDigitalKey(digitalKeyDto: DigitalKeyDto): Flow<Resource<DigitalKey>> =
        flow {
            try {
                emit(Resource.Loading())
                val response = api.validateDigitalKey(digitalKeyDto).toDigitalKey()
                emit(Resource.Success(response))
            } catch (e: HttpException) {
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
            } catch (e: IOException) {
                emit(Resource.Error("Couldn't reach server. Check your internet connection."))
            }
        }

    override fun getAccessPoints(): Flow<Resource<List<AccessPoint>>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getAccessPoints().map { it.toAccessPoint() }
            emit(Resource.Success(response))
        } catch (e: Exception) {
            logger.logError(
                "getAccessPoints",
                "Error fetching access points: ${e.localizedMessage}",
                e
            )
            val localData = homeDao.getAccessPoints().map { it.toAccessPoint() }
            emit(
                Resource.Error(
                    data = localData,
                    message = e.localizedMessage ?: "An unexpected error occured"
                )
            )
        }
    }

    override fun getAllResidents(): Flow<Resource<List<Resident>>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getAllResidents().map { it.toResident() }
            emit(Resource.Success(response))
        } catch (e: Exception) {
            logger.logError("getAllResidents", "Error fetching residents: ${e.localizedMessage}", e)
            val localData = homeDao.getAllResidents().map { it.toResident() }
            emit(Resource.Error(data = localData, message = mapError(e)))
        }
    }

    override fun getKioskData(): Flow<Resource<Main>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getKioskData().toMain()
            emit(Resource.Success(response))
        } catch (e: Exception) {
            logger.logError("getKioskData", "Error fetching kiosk data: ${e.localizedMessage}", e)
            val localData = homeDao.getKioskData()?.toMain()
            emit(Resource.Error(data = localData, message = e.localizedMessage ?: "An unexpected error occured"))
        }
    }

    override fun getLeasingOfficeDetails(): Flow<Resource<LeasingOffice>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getLeasingOfficeDetails().toLeasingOffice()
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            logger.logError(
                "getLeasingOfficeDetails",
                "Error fetching leasing office details: ${e.localizedMessage}",
                e
            )
            val localData = homeDao.getLeasingOfficeDetail()?.toLeasingOffice()
            emit(Resource.Error(data = localData, message = e.localizedMessage ?: "An unexpected error occured"))
        }
    }

    override fun getIntroButtons(): Flow<Resource<List<String>>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getIntroButtons()
            emit(Resource.Success(response))
        } catch (e: Exception) {
            logger.logError(
                "getIntroButtons",
                "Error fetching intro buttons: ${e.localizedMessage}",
                e
            )
            val localData = homeDao.getButtons().map { it.name }
            emit(Resource.Error(data = localData, message = mapError(e)))
        }
    }

    override suspend fun sync() {

        runCatching {
            val remoteAccessPoints = api.getAccessPoints()
            homeDao.clearAllAccessPoints()
            homeDao.insertAccessPoints(remoteAccessPoints.map { it.toEntity() })
        }.onFailure { e ->
            logger.logError(
                "getAllAccessPoints",
                "Error fetching all access points: ${e.localizedMessage}",
                e
            )
        }

        runCatching {
            val remoteKioskData = api.getKioskData()
            homeDao.clearKioskData()
            homeDao.insertKioskData(remoteKioskData.toEntity())
        }.onFailure { e ->
            logger.logError(
                "getKioskData",
                "Error fetching kiosk data: ${e.localizedMessage}",
                e
            )
        }

        runCatching {
            val remoteLeasingOfficeDetails = api.getLeasingOfficeDetails()
            homeDao.clearLeasingOfficeDetail()
            homeDao.insertLeasingOfficeDetail(remoteLeasingOfficeDetails.toEntity())
        }.onFailure { e ->
            logger.logError(
                "getLeasingOfficeDetails",
                "Error fetching leasing office details: ${e.localizedMessage}",
                e
            )
        }

        runCatching {
            val remoteResidents = api.getAllResidents()
            homeDao.clearResidents()
            homeDao.insertResidents(remoteResidents.map { it.toResidentEntity() })
        }.onFailure { e ->
            logger.logError(
                "getAllResidents",
                "Error fetching all residents: ${e.localizedMessage}",
                e
            )
        }

        runCatching {
            val remoteIntroButtons = api.getIntroButtons()
            homeDao.clearIntroButtons()
            homeDao.insertButtons(remoteIntroButtons.map { IntroButtonEntity(name = it) })
        }.onFailure { e ->
            logger.logError(
                "getIntroButtons",
                "Error fetching intro buttons: ${e.localizedMessage}",
                e
            )
        }
    }

    private fun mapError(e: Exception): String = when (e) {
        is IOException -> "Couldn't reach server. Check your internet connection."
        is HttpException -> e.localizedMessage ?: "Unexpected error"
        else -> "Something went wrong"
    }
}