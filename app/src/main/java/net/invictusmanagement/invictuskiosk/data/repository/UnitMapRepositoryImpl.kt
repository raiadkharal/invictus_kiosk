package net.invictusmanagement.invictuskiosk.data.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.FileManager
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.commons.SafeApiCaller
import net.invictusmanagement.invictuskiosk.data.local.dao.VacanciesDao
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.domain.repository.UnitMapRepository
import javax.inject.Inject

class UnitMapRepositoryImpl @Inject constructor(
    private val context: Context,
    private val api: ApiInterface,
    private val vacanciesDao: VacanciesDao,
    private val safeApiCaller: SafeApiCaller
) : UnitMapRepository {
    private val logTag = "UnitMapRepository"

    override suspend fun getMapImage(
        unitId: Long,
        unitMapId: Long,
        toPackageCenter: Boolean
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading())

        val result = safeApiCaller.call(
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
            safeApiCaller.call(
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