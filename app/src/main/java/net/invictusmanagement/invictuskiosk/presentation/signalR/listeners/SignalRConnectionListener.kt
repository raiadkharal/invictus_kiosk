package net.invictusmanagement.invictuskiosk.presentation.signalR.listeners

interface SignalRConnectionListener {
    fun onConnected()
    fun onConnectionError(method: String, e: Exception)
}
