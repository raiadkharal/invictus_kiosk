package net.invictusmanagement.invictuskiosk.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.math.pow

class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val baseDelayMillis: Long = 1000
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var response: Response? = null
        var exception: IOException? = null

        while (attempt < maxRetries) {
            try {
                response = chain.proceed(chain.request())

                if (response.isSuccessful) {
                    return response
                }

                response.close() // release connection
            } catch (e: IOException) {
                exception = e
            }

            attempt++
            val delay = baseDelayMillis * (2.0.pow(attempt.toDouble())).toLong()
            Thread.sleep(delay)
        }

        response?.close()
        throw exception ?: IOException("Failed after $maxRetries retries")
    }
}
