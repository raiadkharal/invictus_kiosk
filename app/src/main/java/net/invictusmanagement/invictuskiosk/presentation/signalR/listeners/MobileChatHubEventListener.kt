package net.invictusmanagement.invictuskiosk.presentation.signalR.listeners

interface MobileChatHubEventListener {
    fun onSendToVoiceMail()
    fun onOpenAccessPoint(relayPort: Int, relayOpenTimer: Int, relayDelayTimer: Int, silent: Boolean)
}
