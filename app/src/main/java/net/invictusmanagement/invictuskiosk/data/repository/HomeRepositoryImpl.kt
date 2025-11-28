package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.commons.SafeApiCaller
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
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val api: ApiInterface,
    private val homeDao: HomeDao,
    private val safeApiCaller: SafeApiCaller
) : HomeRepository {

    private val logTag = "HomeRepository"

    override fun validateDigitalKey(digitalKeyDto: DigitalKeyDto): Flow<Resource<DigitalKey>> = flow {

        emit(Resource.Loading())

        emit(
            safeApiCaller.call(
                tag = "$logTag-validateDigitalKey",
                remoteCall = { api.validateDigitalKey(digitalKeyDto).toDigitalKey() },
                localFallback = null,
                errorMessage = "Failed to validate digital key: ${digitalKeyDto.key}"
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
//                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
//            } catch (e: IOException) {
//                emit(Resource.Error("Couldn't reach server. Check your internet connection."))
//            }
//        }

    override fun getAccessPoints(): Flow<Resource<List<AccessPoint>>> = flow {
        emit(Resource.Loading())

        emit(
            safeApiCaller.call(
                tag = "$logTag-getAccessPoints",
                remoteCall = { api.getAccessPoints().map { it.toAccessPoint() } },
                localFallback = { homeDao.getAccessPoints().map { it.toAccessPoint() } },
                errorMessage = "Failed to fetch access points"
            )
        )
    }

    override fun getAllResidents(): Flow<Resource<List<Resident>>> = flow {
        emit(Resource.Loading())

        emit(
            safeApiCaller.call(
                tag = "$logTag-getAllResidents",
                remoteCall = { api.getAllResidents().map { it.toResident() } },
                localFallback = { homeDao.getAllResidents().map { it.toResident() } },
                errorMessage = "Failed to fetch residents"
            )
        )
    }

    override fun getKioskData(): Flow<Resource<Main?>> = flow {
        emit(Resource.Loading())

        emit(
            safeApiCaller.call(
                tag = "$logTag-getKioskData",
                remoteCall = { api.getKioskData().toMain() },
                localFallback = { homeDao.getKioskData()?.toMain() },
                errorMessage = "Failed to fetch leasing office details"
            )
        )
    }

    override fun getLeasingOfficeDetails(): Flow<Resource<LeasingOffice?>> = flow {
        emit(Resource.Loading())

        emit(
            safeApiCaller.call(
                tag = "$logTag-getLeasingOfficeDetails",
                remoteCall = { api.getLeasingOfficeDetails().toLeasingOffice() },
                localFallback = { homeDao.getLeasingOfficeDetail()?.toLeasingOffice() },
                errorMessage = "Failed to fetch leasing office details"
            )
        )
    }

    override fun getIntroButtons(): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading())

        emit(
            safeApiCaller.call(
                tag = "$logTag-getIntroButtons",
                remoteCall = { api.getIntroButtons() },
                localFallback = { homeDao.getButtons().map { it.name } },
                errorMessage = "Failed to fetch intro buttons"
            )
        )
    }

    override suspend fun sync() {

        safeApiCaller.call(
            tag = "$logTag-sync-accessPoints",
            remoteCall = {
                val remote = api.getAccessPoints()
                homeDao.clearAllAccessPoints()
                homeDao.insertAccessPoints(remote.map { it.toEntity() })
            },
            errorMessage = "Failed to sync access points"
        )

        safeApiCaller.call(
            tag = "$logTag-sync-kioskData",
            remoteCall = {
                val remote = api.getKioskData()
                homeDao.clearKioskData()
                homeDao.insertKioskData(remote.toEntity())
            },
            errorMessage = "Failed to sync kiosk data"
        )

        safeApiCaller.call(
            tag = "$logTag-sync-leasingOfficeDetails",
            remoteCall = {
                val remote = api.getLeasingOfficeDetails()
                homeDao.clearLeasingOfficeDetail()
                homeDao.insertLeasingOfficeDetail(remote.toEntity())
            },
            errorMessage = "Failed to sync leasing office data"
        )

        safeApiCaller.call(
            tag = "$logTag-sync-residents",
            remoteCall = {
                val remote = api.getAllResidents()
                homeDao.clearResidents()
                homeDao.insertResidents(remote.map { it.toResidentEntity() })
            },
            errorMessage = "Failed to sync residents"
        )

        safeApiCaller.call(
            tag = "$logTag-sync-introButtons",
            remoteCall = {
                val remote = api.getIntroButtons()
                homeDao.clearIntroButtons()
                homeDao.insertButtons(remote.map { IntroButtonEntity(name = it) })
            },
            errorMessage = "Failed to sync intro buttons"
        )
    }

    private fun mapError(e: Exception): String = when (e) {
        is IOException -> "Couldn't reach server. Check your internet connection."
        is HttpException -> e.localizedMessage ?: "Unexpected error"
        else -> "Something went wrong"
    }
}