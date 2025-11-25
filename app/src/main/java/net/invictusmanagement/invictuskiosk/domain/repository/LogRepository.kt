package net.invictusmanagement.invictuskiosk.domain.repository

import net.invictusmanagement.invictuskiosk.data.remote.dto.ErrorLogRequestDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.RelayManagerLogDto

interface LogRepository {
    suspend fun postRelayManagerLog(log: RelayManagerLogDto)
    suspend fun postErrorLog(log: ErrorLogRequestDto)
    suspend fun syncPendingLogs()

}
