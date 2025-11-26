package net.invictusmanagement.invictuskiosk.data.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.commons.FileManager
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.commons.safeApiCall
import net.invictusmanagement.invictuskiosk.data.local.dao.VacanciesDao
import net.invictusmanagement.invictuskiosk.data.local.entities.UnitImageEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.toContactRequest
import net.invictusmanagement.invictuskiosk.data.local.entities.toUnit
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.UnitDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.toContactRequest
import net.invictusmanagement.invictuskiosk.data.remote.dto.toUnit
import net.invictusmanagement.invictuskiosk.data.remote.dto.toVacantUnitEntity
import net.invictusmanagement.invictuskiosk.domain.model.ContactRequest
import net.invictusmanagement.invictuskiosk.domain.model.Unit
import net.invictusmanagement.invictuskiosk.domain.model.toEntity
import net.invictusmanagement.invictuskiosk.domain.repository.VacancyRepository
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class VacancyRepositoryImpl @Inject constructor(
    private val context: Context,
    private val api: ApiInterface,
    private val vacanciesDao: VacanciesDao,
    private val logger: GlobalLogger
) : VacancyRepository {

    private val logTag = "VacancyRepository"

    override fun getUnits(): Flow<Resource<List<Unit>>> = flow {
        emit(Resource.Loading())

        emit(
            safeApiCall(
                logger = logger,
                tag = "$logTag-getUnits",
                remoteCall = { api.getUnits().map { it.toUnit() } },
                localFallback = { vacanciesDao.getUnits().map { it.toUnit() } },
                errorMessage = "Failed to load units"
            )
        )
    }


    override fun sendContactRequest(contactRequest: ContactRequest): Flow<Resource<ContactRequest>> =
        flow {
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
        val pending = vacanciesDao.getContactRequests()

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

    override suspend fun sync() {
        safeApiCall(
            logger = logger,
            tag = "$logTag-sync-units",
            remoteCall = {
                val remoteUnits = api.getUnits()
                vacanciesDao.clearUnits()
                vacanciesDao.insertUnits(remoteUnits.map { it.toVacantUnitEntity() })

                // load images for all the units
                loadUnitImages(remoteUnits)
            },
            errorMessage = "Failed to sync units and images."
        )
    }

    private suspend fun loadUnitImages(units: List<UnitDto>) {
        coroutineScope {
            units.forEach { unit ->
                unit.imageIds?.forEach { imageId ->
                    launch(Dispatchers.IO) {
                        try {
                            // skip if the image with same id already exists
                            if (vacanciesDao.getUnitImage(imageId) != null) return@launch

                            // Fetch image bytes from the server
                            val bytes = api.getUnitImage(unit.id.toLong(), imageId).bytes()
                            val imagePath = FileManager.saveImageToCache(context, bytes, "unit_${unit.id}_${imageId}.jpg")

                            // Save Image to the local database
                            vacanciesDao.insertUnitImage(
                                UnitImageEntity(
                                    unitImageId = imageId,
                                    unitId = unit.id,
                                    imagePath = imagePath
                                )
                            )

                        } catch (e: Exception) {
                            logger.logError("sync-preload-images", e.message ?: "error")
                        }
                    }
                }
            }
        }
    }

}