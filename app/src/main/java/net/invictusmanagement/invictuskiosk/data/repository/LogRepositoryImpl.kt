package net.invictusmanagement.invictuskiosk.data.repository

import android.util.Log
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
            fetchLogs = { systemLogDao.getLogsByType(LogType.RELAY_MANAGER) },
            sendLog = { postRelayManagerLog(it.toRelayLog()) }
        )

        syncLogsByType(
            type = LogType.ERROR,
            tag = "ErrorLogs",
            fetchLogs = { systemLogDao.getLogsByType(LogType.ERROR) },
            sendLog = { postErrorLog(it.toErrorLog()) }
        )
    }

    private suspend fun syncLogsByType(
        type: LogType,
        tag: String,
        fetchLogs: suspend () -> List<SystemLogEntity>,
        sendLog: suspend (SystemLogEntity) -> Unit
    ) {
        runCatching {
            val logs = fetchLogs()

            if (logs.isEmpty()) {
                Log.d("Sync$type", "No pending $tag to sync")
                return
            }

            Log.d("Sync$type", "Found ${logs.size} $tag to sync")

            for (log in logs) {
                try {
                    sendLog(log)

                    // On success, delete from DB
                    systemLogDao.deleteLogById(log.id)
                    Log.d("Sync$type", "Synced and removed ID: ${log.id}")

                } catch (e: IOException) {
                    Log.e("Sync$type", "Network error for ID ${log.id}, stopping sync")
                    return
                } catch (e: Exception) {
                    Log.e("Sync$type", "Failed sending ID ${log.id}: ${e.message}")
                }
            }

        }.onFailure { e ->
            Log.e("Sync$type", "Unexpected error syncing $tag: ${e.message}")
        }
    }

}
