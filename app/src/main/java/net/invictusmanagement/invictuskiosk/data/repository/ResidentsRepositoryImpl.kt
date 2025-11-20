package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
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
    private val logger: GlobalLogger
) : ResidentsRepository {

    override fun getResidentsByName(filter: String, byName: String): Flow<Resource<List<Resident>>> = flow{
        try {
            emit(Resource.Loading())
            val response = api.getResidentsByName(filter,byName).map { it.toResident() }
            emit(Resource.Success(response))
        } catch(e: HttpException) {
            logger.logError("getResidentsByName", "Error fetching residents by name ${e.localizedMessage}" , e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch(e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    override fun getResidentsByUnitNumber(unitNumber: String): Flow<Resource<List<Resident>>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getResidentsByUnitNumber(unitNumber).map { it.toResident() }
            emit(Resource.Success(response))
        } catch(e: HttpException) {
            logger.logError("getResidentsByUnitNumber", "Error fetching residents by unit number ${e.localizedMessage}" , e)
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
            logger.logError("validateDigitalKey", "Error validating digital key ${e.localizedMessage}" , e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch(e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    override fun getAllLeasingAgents(byName: String): Flow<Resource<List<Resident>>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getAllLeasingAgents(byName).map { it.toResident() }
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            logger.logError("getAllLeasingAgents", "Error fetching leasing agents by name ${e.localizedMessage}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}