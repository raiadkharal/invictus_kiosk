package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.home.toMain
import net.invictusmanagement.invictuskiosk.data.remote.dto.toAccessPoint
import net.invictusmanagement.invictuskiosk.data.remote.dto.toDigitalKey
import net.invictusmanagement.invictuskiosk.data.remote.dto.toLeasingOffice
import net.invictusmanagement.invictuskiosk.data.remote.dto.toResident
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
    private val logger: GlobalLogger
) : HomeRepository {

    override fun validateDigitalKey(digitalKeyDto: DigitalKeyDto): Flow<Resource<DigitalKey>> =
        flow {
            try {
                emit(Resource.Loading())
                val response = api.validateDigitalKey(digitalKeyDto).toDigitalKey()
                emit(Resource.Success(response))
            } catch (e: HttpException) {
                logger.logError("validateDigitalKey", "Error validating digital key: ${e.localizedMessage}", e)
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
            logger.logError("getAccessPoints", "Error fetching access points: ${e.localizedMessage}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    override fun getAllResidents(): Flow<Resource<List<Resident>>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getAllResidents().map { it.toResident() }
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            logger.logError("getAllResidents", "Error fetching residents: ${e.localizedMessage}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
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
        try {
            emit(Resource.Loading())
            val response = api.getIntroButtons()
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            logger.logError("getIntroButtons", "Error fetching intro buttons: ${e.localizedMessage}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}