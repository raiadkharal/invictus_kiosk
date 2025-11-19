package net.invictusmanagement.invictuskiosk.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.HttpURLConnection
import java.net.URL

class NetworkMonitor(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isConnected = MutableStateFlow(checkInternetCapability())
    val isConnected = _isConnected.asStateFlow()

    private val _isInternetStable = MutableStateFlow(false)
    val isInternetStable = _isInternetStable.asStateFlow()

    private var stabilityJob: Job? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateConnectionState()
            startStabilityChecks()
        }

        override fun onLost(network: Network) {
            _isConnected.value = false
            _isInternetStable.value = false
            stopStabilityChecks()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            updateConnectionState()
        }
    }

    fun startMonitoring() {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        updateConnectionState()

        if (_isConnected.value) {
            startStabilityChecks()
        }
    }

    fun stopMonitoring() {
        stopStabilityChecks()
        runCatching { connectivityManager.unregisterNetworkCallback(networkCallback) }
    }

    private fun updateConnectionState() {
        _isConnected.value = checkInternetCapability()
    }

    private fun startStabilityChecks() {
        if (stabilityJob?.isActive == true) return

        stabilityJob = scope.launch {
            while (isActive) {
                if (_isConnected.value) {
                    _isInternetStable.value = checkInternetReachability()
                } else {
                    _isInternetStable.value = false
                }
                delay(3000) // test every 3 seconds
            }
        }
    }

    private fun stopStabilityChecks() {
        stabilityJob?.cancel()
        stabilityJob = null
    }

    // Network has internet capability
    private fun checkInternetCapability(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Check if the Internet is actually reachable
    private fun checkInternetReachability(): Boolean {
        return try {
            val url = URL("https://clients3.google.com/generate_204")
            (url.openConnection() as HttpURLConnection).run {
                connectTimeout = 1500
                readTimeout = 1500
                requestMethod = "GET"
                connect()
                responseCode == 204
            }
        } catch (e: Exception) {
            false
        }
    }
}
