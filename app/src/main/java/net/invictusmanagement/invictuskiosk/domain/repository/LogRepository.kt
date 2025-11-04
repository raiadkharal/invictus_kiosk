package net.invictusmanagement.invictuskiosk.domain.repository

import kotlinx.coroutines.flow.Flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.dto.RelayManagerLogDto

interface LogRepository {
    fun postKioskLog(log: RelayManagerLogDto): Flow<Resource<RelayManagerLogDto>>
}
