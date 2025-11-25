package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.data.local.entities.LogType
import net.invictusmanagement.invictuskiosk.data.local.entities.SystemLogEntity

data class ErrorLogRequestDto(
    val logger: String,
    val exceptionMessage: String,
    val innerException: String = ""
)

fun ErrorLogRequestDto.toEntity(): SystemLogEntity {
    return SystemLogEntity(
        logger = this.logger,
        exceptionMessage = this.exceptionMessage,
        innerException = this.innerException,
        logType = LogType.ERROR
    )
}
