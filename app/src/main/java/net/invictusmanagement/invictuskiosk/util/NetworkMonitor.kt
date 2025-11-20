package net.invictusmanagement.invictuskiosk.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
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
            checkRealInternetConnection()
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
        checkRealInternetConnection()

        startPeriodicChecks()
    }

    fun stopMonitoring() {
        isMonitoring = false

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {}

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

    private fun checkRealInternetConnection() {
        val activeNetwork = connectivityManager.activeNetwork
        if (activeNetwork == null) {
            _isConnected.value = false
            return
        }

        scope.launch {
            val result = pingGoogle204()
            _isConnected.value = result
        }
    }

    private fun pingGoogle204(): Boolean {
        val request = Request.Builder()
            .url("https://connectivitycheck.gstatic.com/generate_204")
            .build()

        return try {
            val response = client.newCall(request).execute()
            response.code == 204 || response.code == 200
        } catch (e: Exception) {
            false
        }
    }
}
