package net.invictusmanagement.invictuskiosk.presentation.signalR

import android.util.Log
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.TransportEnum
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import net.invictusmanagement.invictuskiosk.BuildConfig
import net.invictusmanagement.invictuskiosk.presentation.signalR.listeners.SignalRConnectionListener
import net.invictusmanagement.invictuskiosk.presentation.signalR.listeners.MobileChatHubEventListener
import net.invictusmanagement.invictuskiosk.util.NetworkMonitor
import java.util.concurrent.atomic.AtomicBoolean

class MobileChatHubManager(
    private val kioskId: Int,
    private val listener: MobileChatHubEventListener,
    private val connectionListener: SignalRConnectionListener,
    private val networkMonitor: NetworkMonitor
) {

    private val TAG = "MobileChatHubManager"
    private var hubConnection: HubConnection? = null
    private val reconnecting = AtomicBoolean(false)
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    companion object {
        private var lastInstance: MobileChatHubManager? = null
    }

    init {
        // Stop previous instance before this new one becomes active
        lastInstance?.cleanupInternal()
        lastInstance = this
        Log.d(TAG, "New SignalR manager instance created, old instance cleaned.")
    }

    /**
     * Initializes and connects to SignalR in the background
     */
    suspend fun connect() {
        if(networkMonitor.isConnected.firstOrNull() == false) return

        if (hubConnection != null && hubConnection?.connectionState?.name == "CONNECTED") {
            Log.d(TAG, "connect: Already connected")
            connectionListener.onConnected()
            return
        }

        hubConnection = HubConnectionBuilder
            .create(BuildConfig._chatMobileHubBaseUrl)
            .withTransport(TransportEnum.LONG_POLLING)
            .build()

        registerHandlers()

        coroutineScope.launch {
            try {
                Log.d(TAG, "connect: Connecting to SignalR...")
                hubConnection?.start()?.blockingAwait()
                registerToHub()
                connectionListener.onConnected()
                Log.d(TAG, "connect: SignalR connected and registered (Kiosk $kioskId)")
            } catch (e: Exception) {
                connectionListener.onConnectionError("connect",e)
                Log.e(TAG, "Error connecting to SignalR: ${e.message}")
                scheduleReconnect()
            }
        }
    }

    /**
     * Registers the kiosk to the hub
     */
    private suspend fun registerToHub() {
        withContext(Dispatchers.IO) {
            try {
                hubConnection?.invoke("Register", kioskId.toLong())
                Log.d(TAG, "registerToHub: Kiosk registered successfully")
            } catch (e: Exception) {
                connectionListener.onConnectionError("registerToHub",e)
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
            connectionListener.onConnected()
        }, String::class.java)

        hubConnection?.on("SendToVoiceMail", {
            listener.onSendToVoiceMail()
        })

        hubConnection?.on("Disconnected", { message: String? ->
            Log.d(TAG, "Disconnected event from server: $message")
            scheduleReconnect()
        }, String::class.java)

        hubConnection?.onClosed { error ->
            Log.d(TAG, "SignalR connection closed: ${error?.message ?: "no error"}")
            scheduleReconnect()
        }

        hubConnection?.on(
            "OpenAccessPoint",
            { relayPort: String, relayOpenTimer: String, relayDelayTimer: String, silent: Boolean ->
                Log.d(
                    TAG,
                    "Open access point: port=$relayPort open=$relayOpenTimer delay=$relayDelayTimer silent=$silent"
                )


                listener.onOpenAccessPoint(relayPort.toInt(), relayOpenTimer.toInt(), relayDelayTimer.toInt(), silent)
            },
            String::class.java,
            String::class.java,
            String::class.java,
            Boolean::class.java
        )
    }

    /**
     * Attempts to reconnect after a short delay if disconnected
     */
    private fun scheduleReconnect() {
        if (reconnecting.getAndSet(true)) return // prevent multiple reconnections

        coroutineScope.launch {
            Log.d(TAG, "Attempting to reconnect in 10 seconds...")
            delay(10000)
            reconnecting.set(false)
            connect()
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
        if (lastInstance == this) lastInstance = null
    }

    private fun safeStop() {
        try {
            val conn = hubConnection ?: return

            if (conn.connectionState.name == "CONNECTED") {
                conn.stop()
            } else {
                Log.w(TAG, "safeStop: Ignored stop() because connection never completed init")
            }

        } catch (e: Exception) {
            Log.e(TAG, "safeStop: SignalR stop() crashed internally → ignored: ${e.message}")
        }
    }

}
