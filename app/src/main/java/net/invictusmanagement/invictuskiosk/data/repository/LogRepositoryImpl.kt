package net.invictusmanagement.invictuskiosk.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.invictusmanagement.invictuskiosk.data.local.dao.SystemLogDao
import net.invictusmanagement.invictuskiosk.data.local.entities.LogType
import net.invictusmanagement.invictuskiosk.data.local.entities.SystemLogEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.toErrorLog
import net.invictusmanagement.invictuskiosk.data.local.entities.toRelayLog
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.MobileApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.ErrorLogRequestDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.RelayManagerLogDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.toEntity
import net.invictusmanagement.invictuskiosk.domain.repository.LogRepository
import java.io.IOException
import javax.inject.Inject

class LogRepositoryImpl @Inject constructor(
    private val mobileApi: MobileApiInterface,
    private val api: ApiInterface,
    private val systemLogDao: SystemLogDao,
) : LogRepository {

    override suspend fun postRelayManagerLog(log: RelayManagerLogDto) {
        try {
            mobileApi.postLog(log)
        } catch (e: Exception) {
            e.printStackTrace()
            // Save locally
            systemLogDao.insertLog(log.toEntity())
        }
    }


    override suspend fun postErrorLog(log: ErrorLogRequestDto){
        try {
            api.addErrorLog(log)
        } catch (e: Exception) {
            e.printStackTrace()
            systemLogDao.insertLog(log.toEntity())
        }
    }

    override suspend fun syncPendingLogs() {

        syncLogsByType(
            type = LogType.RELAY_MANAGER,
            tag = "RelayManagerLogs",
            fetchBatch = { limit -> systemLogDao.getLogsByTypeBatch(LogType.RELAY_MANAGER, limit) },
            sendLog = { postRelayManagerLog(it.toRelayLog()) }
        )

        syncLogsByType(
            type = LogType.ERROR,
            tag = "ErrorLogs",
            fetchBatch = { limit -> systemLogDao.getLogsByTypeBatch(LogType.ERROR, limit) },
            sendLog = { postErrorLog(it.toErrorLog()) }
        )
    }

    private suspend fun syncLogsByType(
        type: LogType,
        tag: String,
        fetchBatch: suspend (Int) -> List<SystemLogEntity>,
        sendLog: suspend (SystemLogEntity) -> Unit
    ) {
        val batchSize = 100
        withContext(Dispatchers.IO) {
            try {
                Log.d("Sync$type", "Starting sync for $tag")

                while (true) {
                    val batch = fetchBatch(batchSize)
                    if (batch.isEmpty()) {
                        Log.d("Sync$type", "No more $tag to sync")
                        break
                    }

                    Log.d("Sync$type", "Syncing batch of ${batch.size} $tag")

                    for (log in batch) {
                        try {
                            sendLog(log)
                            systemLogDao.deleteLogById(log.id) // on success, remove the row
                            Log.d("Sync$type", "Synced and removed ID: ${log.id}")
                        } catch (io: IOException) {
                            Log.e("Sync$type", "Network error for ID ${log.id}, stopping sync: ${io.message}")
                            return@withContext
                        } catch (e: Exception) {
                            Log.e("Sync$type", "Failed sending ID ${log.id}: ${e.message}")
                            systemLogDao.deleteLogById(log.id) // we choose to drop to avoid infinite retry storms
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Sync$type", "Unexpected error syncing $tag: ${e.message}")
            }
        }
    }

}
