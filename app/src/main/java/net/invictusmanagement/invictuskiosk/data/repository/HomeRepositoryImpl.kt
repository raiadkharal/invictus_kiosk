package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.local.dao.HomeDao
import net.invictusmanagement.invictuskiosk.data.local.entities.IntroButtonEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.toResident
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.home.toMain
import net.invictusmanagement.invictuskiosk.data.remote.dto.toAccessPoint
import net.invictusmanagement.invictuskiosk.data.remote.dto.toDigitalKey
import net.invictusmanagement.invictuskiosk.data.remote.dto.toLeasingOffice
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
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

//    override fun getAllResidents(): Flow<Resource<List<Resident>>> = flow {
//        try {
//            emit(Resource.Loading())
//            val response = api.getAllResidents().map { it.toResident() }
//            emit(Resource.Success(response))
//        } catch (e: HttpException) {
//            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
//        } catch (e: IOException) {
//            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
//        }
//    }

    override fun getAllResidents(): Flow<Resource<List<Resident>>> = flow {
        emit(Resource.Loading())

        try {
            fetchResidents()
        } catch (e: Exception) {
            emit(Resource.Error(mapError(e)))
        }

        emitAll(
            homeDao.getAllResidents().map { list ->
                Resource.Success(list.map { it.toResident() })
            }
        )
    }

    private suspend fun fetchResidents() {
        val remote = api.getAllResidents()
        homeDao.clearResidents()
        homeDao.insertResidents(remote.map { it.toResidentEntity() })
    }

    override fun getKioskData(): Flow<Resource<Main>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getKioskData().toMain()
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            logger.logError("getKioskData", "Error fetching kiosk data: ${e.localizedMessage}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    override fun getLeasingOfficeDetails(): Flow<Resource<LeasingOffice>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getLeasingOfficeDetails().toLeasingOffice()
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            logger.logError("getLeasingOfficeDetails", "Error fetching leasing office details: ${e.localizedMessage}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    override fun getIntroButtons(): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading())

        try {
            fetchIntroButtons()
        } catch (e: Exception) {
            emit(Resource.Error(mapError(e)))
        }

        emitAll(
            homeDao.getButtons().map { list ->
                Resource.Success(list.map { it.name })
            }
        )
    }

    private suspend fun fetchIntroButtons() {
        val remote = api.getIntroButtons()
        homeDao.clearIntroButtons()
        homeDao.insertButtons(remote.map { IntroButtonEntity(name = it) })
    }


    private fun mapError(e: Exception): String = when (e) {
        is IOException -> "Couldn't reach server. Check your internet connection."
        is HttpException -> e.localizedMessage ?: "Unexpected error"
        else -> "Something went wrong"
    }
}