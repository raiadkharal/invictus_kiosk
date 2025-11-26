package net.invictusmanagement.invictuskiosk.data.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.FileManager
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.commons.safeApiCall
import net.invictusmanagement.invictuskiosk.data.local.dao.VacanciesDao
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.domain.repository.UnitMapRepository
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import javax.inject.Inject

class UnitMapRepositoryImpl @Inject constructor(
    private val context: Context,
    private val api: ApiInterface,
    private val logger: GlobalLogger,
    private val vacanciesDao: VacanciesDao
) : UnitMapRepository {
    private val logTag = "UnitMapRepository"

    override suspend fun getMapImage(
        unitId: Long,
        unitMapId: Long,
        toPackageCenter: Boolean
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading())

        val result = safeApiCall(
            logger = logger,
            tag = "$logTag-getMapImage",
            remoteCall = {
                val bytes = api.getMapImage(unitId, unitMapId, toPackageCenter).bytes()
                FileManager.saveImageToCache(context,bytes, "map_${unitId}_${unitMapId}.jpg")
            },
            localFallback = null,
            errorMessage = "Failed to load map image"
        )

        emit(result)
    }

    override suspend fun getUnitImage(
        unitId: Long,
        unitImageId: Long
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading())

        emit(
            safeApiCall(
                logger = logger,
                tag = "$logTag-getUnitImage",
                remoteCall = {
                   val bytes = api.getUnitImage(unitId, unitImageId).bytes()
                    FileManager.saveImageToCache(context,bytes, "unit_${unitId}_${unitImageId}.jpg")
                },
                localFallback = { vacanciesDao.getUnitImage(unitImageId)?.imagePath ?: ""},
                errorMessage = "Failed to load unit image"
            )
        )
    }

}