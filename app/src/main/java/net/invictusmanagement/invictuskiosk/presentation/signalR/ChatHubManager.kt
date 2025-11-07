package net.invictusmanagement.invictuskiosk.presentation.signalR

import android.util.Log
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.TransportEnum
import kotlinx.coroutines.*
import net.invictusmanagement.invictuskiosk.BuildConfig
import net.invictusmanagement.invictuskiosk.presentation.signalR.listeners.ChatHubEventListener
import net.invictusmanagement.invictuskiosk.presentation.signalR.listeners.SignalRConnectionListener
import net.invictusmanagement.invictuskiosk.presentation.signalR.listeners.MobileChatHubEventListener
import java.util.concurrent.atomic.AtomicBoolean

class ChatHubManager(
    private val kioskId: Int,
    private val listener: ChatHubEventListener
) {

    private val TAG = "SignalRManager"
    private var hubConnection: HubConnection? = null
    private val reconnecting = AtomicBoolean(false)
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Initializes and connects to SignalR in the background
     */
    fun connect() {
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
                registerToHub()
                Log.d(TAG, "connect: SignalR connected and registered (Kiosk $kioskId)")
            } catch (e: Exception) {
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
                hubConnection?.invoke("RegisterVideoGroup", kioskId.toLong())
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
     * Cleanly disconnects from SignalR
     */
   private fun disconnect() {
        coroutineScope.launch {
            try {
                Log.d(TAG, "disconnect: Stopping SignalR connection...")
                hubConnection?.stop()
                hubConnection = null
            } catch (e: Exception) {
                Log.e(TAG, "disconnect: Error stopping SignalR: ${e.message}")
            }
        }
    }

    /**
     * Cancels all coroutines when ViewModel or lifecycle is cleared
     */
    fun cleanup() {
        coroutineScope.cancel()
        disconnect()
    }
}
