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

        val fallback = localFallback?.invoke()

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
