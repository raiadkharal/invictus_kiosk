package net.invictusmanagement.invictuskiosk.commons

import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import net.invictusmanagement.invictuskiosk.util.NetworkMonitor


class SafeApiCaller @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val logger: GlobalLogger
) {

    suspend fun <T> call(
        tag: String,
        remoteCall: suspend () -> T,
        localFallback: (suspend () -> T)? = null,
        errorMessage: String = "An error occurred"
    ): Resource<T> {

        val isConnected = networkMonitor.isConnected.first()

        //Instant offline fallback (no try/catch needed)
        if (!isConnected) {
            logger.logError(tag, "No internet connection. Falling back to local.")

            val fallback = runCatching { localFallback?.invoke() }.getOrNull()
            return when {
                fallback != null -> Resource.Error(
                    data = fallback,
                    message = "No internet connection. Falling back to local."
                )
                else ->
                    Resource.Error(
                        message = "No internet connection. No local data available."
                    )
            }
        }

        return safeApiCall(
            logger = logger,
            tag = tag,
            remoteCall = remoteCall,
            localFallback = localFallback,
            errorMessage = errorMessage
        )
    }
}


private suspend fun <T> safeApiCall(
    logger: GlobalLogger,
    tag: String,
    remoteCall: suspend () -> T,
    localFallback: (suspend () -> T)? = null,
    errorMessage: String = "An error occurred"
): Resource<T> {
    return try {
        Resource.Success(remoteCall())
    } catch (e: Exception) {
        logger.logError(tag, "$errorMessage: ${e.localizedMessage}", e)

        val fallback = runCatching { localFallback?.invoke() }.getOrNull()

        if (fallback != null) {
            Resource.Error(
                data = fallback,
                message = e.localizedMessage ?: errorMessage
            )
        } else {
            Resource.Error(
                message = e.localizedMessage ?: errorMessage
            )
        }
    }
}
