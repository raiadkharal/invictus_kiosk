package net.invictusmanagement.invictuskiosk.presentation.signalR.listeners

interface ChatHubEventListener {
    fun onOpenAccessPoint(relayPort: Int, relayOpenTimer: Int, relayDelayTimer: Int, silent: Boolean)
}
