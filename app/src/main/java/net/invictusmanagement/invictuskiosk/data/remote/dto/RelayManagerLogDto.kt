package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.data.local.entities.LogType
import net.invictusmanagement.invictuskiosk.data.local.entities.SystemLogEntity

data class RelayManagerLogDto(
    val logger: String,
    val exceptionMessage: String,
    val innerException: String,
    val kioskActivationCode: String
)

fun RelayManagerLogDto.toEntity(): SystemLogEntity {
    return SystemLogEntity(
        logger = this.logger,
        exceptionMessage = this.exceptionMessage,
        innerException = this.innerException,
        kioskActivationCode = this.kioskActivationCode,
        logType = LogType.RELAY_MANAGER
    )
}
