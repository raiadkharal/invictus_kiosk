package net.invictusmanagement.invictuskiosk.data.repository

import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.MobileApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.ErrorLogRequestDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.RelayManagerLogDto
import net.invictusmanagement.invictuskiosk.domain.repository.LogRepository
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import javax.inject.Inject

class LogRepositoryImpl @Inject constructor(
    private val mobileApi: MobileApiInterface,
    private val api: ApiInterface,
    private val logger: GlobalLogger
) : LogRepository {

    override suspend fun postRelayManagerLog(log: RelayManagerLogDto) {
        try {
            mobileApi.postLog(log)
        } catch (e: Exception) {
            logger.logError("postRelayManagerLog", "Error posting relay manager log ${e.localizedMessage}", e)
            e.printStackTrace()
        }
    }


    override suspend fun postErrorLog(log: ErrorLogRequestDto){
        try {
            api.addErrorLog(log)
        } catch (e: Exception) {
            logger.logError("postErrorLog", "Error posting error log ${e.localizedMessage}", e)
            e.printStackTrace()
        }
    }
}
