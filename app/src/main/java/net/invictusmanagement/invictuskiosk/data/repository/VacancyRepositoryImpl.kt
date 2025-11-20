package net.invictusmanagement.invictuskiosk.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.local.dao.VacanciesDao
import net.invictusmanagement.invictuskiosk.data.local.entities.toContactRequest
import net.invictusmanagement.invictuskiosk.data.local.entities.toUnit
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.toContactRequest
import net.invictusmanagement.invictuskiosk.data.remote.dto.toVacantUnitEntity
import net.invictusmanagement.invictuskiosk.domain.model.ContactRequest
import net.invictusmanagement.invictuskiosk.domain.model.Unit
import net.invictusmanagement.invictuskiosk.domain.model.toEntity
import net.invictusmanagement.invictuskiosk.domain.repository.VacancyRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class VacancyRepositoryImpl @Inject constructor(
    private val api: ApiInterface,
    private val vacanciesDao: VacanciesDao
):VacancyRepository {

    override fun getUnits(): Flow<Resource<List<Unit>>> = flow {
        emit(Resource.Loading())

        try {
            // Try fetching from remote
            fetchUnits()
        } catch (e: Exception) {
            // Log but never block local data
            Log.d("getUnits", mapError(e))
        }

        // Always return the local cached database list
        emitAll(
            vacanciesDao.getUnits().map { list ->
                Resource.Success(list.map { it.toUnit() })
            }
        )
    }

    private suspend fun fetchUnits() {
        val remote = api.getUnits()

        vacanciesDao.clearUnits()
        vacanciesDao.insertUnits(remote.map { it.toVacantUnitEntity() })
    }



//    override fun sendContactRequest(contactRequest: ContactRequest): Flow<Resource<ContactRequest>> = flow{
//        try {
//            emit(Resource.Loading())
//            val response = api.sendContactRequest(contactRequest).toContactRequest()
//            emit(Resource.Success(response))
//        } catch(e: HttpException) {
//            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
//        } catch(e: IOException) {
//            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
//        }
//    }

    override fun sendContactRequest(contactRequest: ContactRequest): Flow<Resource<ContactRequest>> = flow {
        emit(Resource.Loading())

        try {
            val response = api.sendContactRequest(contactRequest).toContactRequest()
            emit(Resource.Success(response))

        } catch (e: IOException) {
            // NETWORK or CONNECTIVITY ERROR → Save locally
            try {
                vacanciesDao.insertContactRequest(contactRequest.toEntity())
                Log.e("sendContactRequest", "Network error → saved locally for retry later")
            } catch (dbErr: Exception) {
                Log.e("sendContactRequest", "Local save failed: ${dbErr.message}")
            }

            emit(Resource.Success(contactRequest)) // Return local version

        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "Server error occurred"))

        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    override suspend fun syncPendingRequests() {
        val pending = vacanciesDao.getPendingRequests()

        if (pending.isEmpty()) {
            Log.d("SyncPendingRequests", "No pending requests to sync")
            return
        }

        Log.d("SyncPendingRequests", "Found ${pending.size} pending requests")

        for (item in pending) {
            try {
                val request = item.toContactRequest()

                api.sendContactRequest(request)

                // Success → remove from local DB
                vacanciesDao.deleteRequest(item.localId)

                Log.d("SyncPendingRequests", "Synced and removed ID: ${item.localId}")

            } catch (e: IOException) {
                Log.e("SyncPendingRequests", "Network error for ID ${item.localId}, stop sync")
                return
            } catch (e: Exception) {
                Log.e("SyncPendingRequests", "Failed sending ID ${item.localId}: ${e.message}")
            }
        }
    }


    private fun mapError(e: Exception): String = when (e) {
        is IOException -> "Couldn't reach server. Check your internet connection."
        is HttpException -> e.localizedMessage ?: "Unexpected error"
        else -> "Something went wrong"
    }
}