package net.invictusmanagement.invictuskiosk.util

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.data.remote.dto.ErrorLogRequestDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.RelayManagerLogDto
import net.invictusmanagement.invictuskiosk.domain.repository.LogRepository
import net.invictusmanagement.relaymanager.util.ILogger
import javax.inject.Inject

class GlobalLogger @Inject constructor(
    private val logRepository: LogRepository,
    private val dataStoreManager: DataStoreManager
): ILogger {

    override fun log(logger: String, message: String, exception: Throwable?) {
        Log.d(logger, message, exception)

        CoroutineScope(Dispatchers.IO).launch {
            val activationCode = dataStoreManager.activationCodeFlow.firstOrNull()
            val logDto = RelayManagerLogDto(
                logger = logger,
                exceptionMessage = message,
                innerException = exception?.stackTraceToString()?.take(4000) ?: "",
                kioskActivationCode = activationCode ?: ""
            )
            logRepository.postRelayManagerLog(logDto)
        }
    }

    override fun logError(logger: String, message: String, exception: Throwable?) {
        Log.d("ERROR-$logger", message, exception)

        CoroutineScope(Dispatchers.IO).launch {
            val activationCode = dataStoreManager.activationCodeFlow.firstOrNull()

            val errorLog = ErrorLogRequestDto(
                logger = "AndroidKiosk/$logger",
                exceptionMessage = message,
                innerException = exception?.stackTraceToString()?.take(4000) ?: "",
                userId = 0,
                locationId = 0,
                role = activationCode ?: ""
            )

            logRepository.postErrorLog(errorLog)
        }
    }
}
