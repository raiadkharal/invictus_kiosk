package net.invictusmanagement.invictuskiosk.presentation.signalR

import android.util.Log
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.TransportEnum
import net.invictusmanagement.invictuskiosk.BuildConfig

class SignalRManager(
    private val kioskId: Int,
    private val listener: SignalREventListener
) {

    private val TAG = "SignalRManager"
    private var hubConnection: HubConnection? = null

    fun connect() {
        hubConnection = HubConnectionBuilder
            .create(BuildConfig._chatMobileHubBaseUrl)
            .withTransport(TransportEnum.LONG_POLLING)
            .build()

        receiveSignalFromServer()


        try {
            hubConnection?.start()?.blockingAwait()
            registerToHub()
        } catch (e: Exception) {
            Log.d("SignalR", "connect: Error connecting to SignalR: ${e.message}")
        }
    }

    private fun registerToHub() {
        try {
            hubConnection?.invoke("Register", kioskId.toLong())
        } catch (e: Exception) {
            Log.d(TAG, "registerToHub: Failed to register kiosk: ${e.message}")
        }
    }

    private fun receiveSignalFromServer() {
        hubConnection?.on("Connected", { message: String? ->
            Log.d(TAG, "connect: Connected event from server: $message")
        }, String::class.java)

        hubConnection?.on("SendToVoiceMail",  {
            listener.onSendToVoiceMail()
        })

        hubConnection?.on("Disconnected", { message: String? ->
            Log.d(TAG, "connect: Disconnected event from server: $message")
        }, String::class.java)

        hubConnection?.onClosed { error ->
            Log.d(TAG, "connect: SignalR connection closed: ${error?.message ?: "no error"}")
        }
    }

    fun disconnect() {
        println("Disconnecting SignalR...")
        hubConnection?.stop()
    }
}
