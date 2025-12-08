package net.invictusmanagement.invictuskiosk.presentation.signalR

import android.util.Log
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.TransportEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.invictusmanagement.invictuskiosk.BuildConfig
import net.invictusmanagement.invictuskiosk.util.NetworkMonitor
import java.util.concurrent.atomic.AtomicBoolean

class ChatHubManager(
    private val groupName: String,
    private val networkMonitor: NetworkMonitor
) {

    private val TAG = "ChatHubManager"
    private var hubConnection: HubConnection? = null
    private val reconnecting = AtomicBoolean(false)
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    companion object {
        private var lastInstance: ChatHubManager? = null
    }

    init {
        // Stop previous instance before this new one becomes active
        lastInstance?.cleanupInternal()
        lastInstance = this
        Log.d(TAG, "New SignalR manager instance created, old instance cleaned.")
    }

    suspend fun connect() {
        if(networkMonitor.isConnected.firstOrNull() == false) return

        if (hubConnection != null && hubConnection?.connectionState?.name == "CONNECTED") {
            Log.d(TAG, "connect: Already connected")
            return
        }

        hubConnection = HubConnectionBuilder
            .create(BuildConfig._chatHubBaseUrl)
            .withTransport(TransportEnum.LONG_POLLING)
            .build()

        registerHandlers()

        coroutineScope.launch {
            try {
                Log.d(TAG, "connect: Connecting to SignalR...")
                hubConnection?.start()?.blockingAwait()
                registerToHub(groupName)
                Log.d(TAG, "connect: SignalR connected and registered (Kiosk $groupName)")
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting to SignalR: ${e.message}")
                scheduleReconnect()
            }
        }
    }

    /**
     * Registers the kiosk to the hub
     */
    private suspend fun registerToHub(groupName: String) {
        withContext(Dispatchers.IO) {
            try {
                hubConnection?.invoke("RegisterVideoGroup", groupName)
                Log.d(TAG, "registerToHub: Kiosk registered successfully")
            } catch (e: Exception) {
                Log.e(TAG, "registerToHub: Failed to register kiosk: ${e.message}")
            }
        }
    }

    /**
     * Listen to server events
     */
    private fun registerHandlers() {
        hubConnection?.on("Connected", { message: String? ->
            Log.d(TAG, "Connected event from server: $message")
        }, String::class.java)


        hubConnection?.on("Disconnected", { message: String? ->
            Log.d(TAG, "Disconnected event from server: $message")
            scheduleReconnect()
        }, String::class.java)

        hubConnection?.onClosed { error ->
            Log.d(TAG, "SignalR connection closed: ${error?.message ?: "no error"}")
            scheduleReconnect()
        }
    }

    /**
     * Attempts to reconnect after a short delay if disconnected
     */
    private fun scheduleReconnect() {
        if (reconnecting.getAndSet(true)) return // prevent multiple reconnections

        coroutineScope.launch {
            Log.d(TAG, "Attempting to reconnect in 5 seconds...")
            delay(5000)
            reconnecting.set(false)
            connect()
        }
    }

    /**
     * send the disconnect signal to mobile app
     */
    fun endVideoCallInMobile() {
        coroutineScope.launch {
            try {
                hubConnection?.invoke("EndVideoCallInMobile", groupName)
                Log.d(TAG, "Sent EndVideoCallInMobile to hub for id: $groupName")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send EndVideoCallInMobile: ${e.message}")
            }
        }
    }


    /**
     * Cleanly disconnects from SignalR
     */
   private fun disconnect() {
        coroutineScope.launch {
            try {
                Log.d(TAG, "disconnect: Stopping SignalR connection...")
                safeStop()
                hubConnection = null
            } catch (e: Exception) {
                Log.e(TAG, "disconnect: Error stopping SignalR: ${e.message}")
            }
        }
    }

    /**
     * Internal cleanup used by companion object (non-cancelled scope)
     */
    private fun cleanupInternal() {
        try {
            Log.d(TAG, "cleanupInternal: Cleaning previous SignalR instance")
            safeStop()
        } catch (_: Exception) {
        }
    }

    /**
     * Cancels all coroutines when ViewModel or lifecycle is cleared
     */
    fun cleanup() {
        coroutineScope.cancel()
        disconnect()
    }

    private fun safeStop() {
        try {
            val conn = hubConnection ?: return

            if (conn.connectionState.name == "CONNECTED") {
                conn.stop()
            } else {
                // connection never fully started → don't call stop()
                Log.w(TAG, "safeStop: Ignored stop() because connection never completed init")
            }

        } catch (e: Exception) {
            Log.e(TAG, "safeStop: SignalR stop() crashed internally → ignored: ${e.message}")
        }
    }
}
