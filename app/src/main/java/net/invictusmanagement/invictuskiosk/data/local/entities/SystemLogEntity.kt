package net.invictusmanagement.invictuskiosk.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.invictusmanagement.invictuskiosk.data.remote.dto.ErrorLogRequestDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.RelayManagerLogDto

@Entity(tableName = "system_logs")
data class SystemLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val logger: String,
    val exceptionMessage: String,
    val innerException: String = "",
    val kioskActivationCode: String? = null,
    val logType: LogType = LogType.ERROR,
    val createdAt: Long = System.currentTimeMillis()
)

fun SystemLogEntity.toErrorLog(): ErrorLogRequestDto{
    return ErrorLogRequestDto(
        logger = logger,
        exceptionMessage = exceptionMessage,
        innerException = innerException
    )
}

fun SystemLogEntity.toRelayLog(): RelayManagerLogDto{
    return RelayManagerLogDto(
        logger = logger,
        exceptionMessage = exceptionMessage,
        innerException = innerException,
        kioskActivationCode = kioskActivationCode ?: ""
    )
}
enum class LogType {
    ERROR,
    RELAY_MANAGER
}