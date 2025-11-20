package net.invictusmanagement.invictuskiosk.data.repository

import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.MobileApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.ErrorLogRequestDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.RelayManagerLogDto
import net.invictusmanagement.invictuskiosk.domain.repository.LogRepository
import javax.inject.Inject

class LogRepositoryImpl @Inject constructor(
    private val mobileApi: MobileApiInterface,
    private val api: ApiInterface
) : LogRepository {

    override suspend fun postRelayManagerLog(log: RelayManagerLogDto) {
        try {
            mobileApi.postLog(log)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override suspend fun postErrorLog(log: ErrorLogRequestDto){
        try {
            api.addErrorLog(log)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
