package net.invictusmanagement.invictuskiosk.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class NetworkMonitor(
    private val context: Context,
    private val logger: GlobalLogger
) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private var periodicJob: Job? = null
    private var isMonitoring = false

    private val _isConnected = MutableStateFlow(true)
    val isConnected = _isConnected.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            logger.logError("NetworkMonitor", "Network available", null)
            scope.launch { checkRealInternetConnection() }
        }

        override fun onLost(network: Network) {
            logger.logError("NetworkMonitor", "Network lost", null)
            _isConnected.value = false
        }
    }

    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        scope.launch {
            delay(500)
            checkRealInternetConnection()
        }
        startPeriodicChecks()
    }

    fun stopMonitoring() {
        isMonitoring = false

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
        }

        periodicJob?.cancel()
    }

    /**
     * Runs every 5 seconds to validate internet connectivity
     */
    private fun startPeriodicChecks() {
        periodicJob?.cancel()

        periodicJob = scope.launch {
            while (isActive) {
                checkRealInternetConnection()
                delay(5000) // 5 seconds
            }
        }
    }

    private suspend fun checkRealInternetConnection() {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities == null || !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            _isConnected.value = false
            return
        }


        val result = pingGoogle204WithRetry()
        _isConnected.value = result
    }

    private suspend fun pingGoogle204WithRetry(
        retries: Int = 3,
        delayMs: Long = 300
    ): Boolean {
        repeat(retries - 1) {
            if (pingGoogle204()) return true
            delay(delayMs)
        }
        return pingGoogle204()
    }

    private suspend fun pingGoogle204(): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://connectivitycheck.gstatic.com/generate_204")
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.code != 204 && response.code != 200) logger.logError(
                "NetworkMonitor",
                "Ping returned Status Code:${response.code} message:${response.message}"
            )
            response.code == 204 || response.code == 200
        } catch (e: Exception) {
            logger.logError("NetworkMonitor", "Ping failed ${e.localizedMessage}", null)
            false
        }
    }
}
