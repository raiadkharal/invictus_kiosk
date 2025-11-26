package net.invictusmanagement.invictuskiosk.commons
import net.invictusmanagement.invictuskiosk.util.GlobalLogger

suspend fun <T> safeApiCall(
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

        val fallbackResult = runCatching { localFallback?.invoke() }

        fallbackResult.fold(
            onSuccess = { fallback ->
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
            },
            onFailure = { fallbackException ->
                logger.logError(tag, "Fallback failed: ${fallbackException.localizedMessage}", fallbackException)
                Resource.Error(
                    message = e.localizedMessage ?: errorMessage
                )
            }
        )
    }
}
