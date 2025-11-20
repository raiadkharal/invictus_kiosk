package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.toContactRequest
import net.invictusmanagement.invictuskiosk.data.remote.dto.toUnit
import net.invictusmanagement.invictuskiosk.domain.model.ContactRequest
import net.invictusmanagement.invictuskiosk.domain.model.Unit
import net.invictusmanagement.invictuskiosk.domain.repository.VacancyRepository
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class VacancyRepositoryImpl @Inject constructor(
    private val api: ApiInterface,
    private val logger: GlobalLogger
):VacancyRepository {

    override fun getUnits(): Flow<Resource<List<Unit>>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getUnits().map { it.toUnit() }
            emit(Resource.Success(response))
        } catch(e: HttpException) {
            logger.logError("getUnits", "Error fetching units ${e.localizedMessage}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch(e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    override fun sendContactRequest(contactRequest: ContactRequest): Flow<Resource<ContactRequest>> = flow{
        try {
            emit(Resource.Loading())
            val response = api.sendContactRequest(contactRequest).toContactRequest()
            emit(Resource.Success(response))
        } catch(e: HttpException) {
            logger.logError("sendContactRequest", "Error sending contact request ${e.localizedMessage}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch(e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}