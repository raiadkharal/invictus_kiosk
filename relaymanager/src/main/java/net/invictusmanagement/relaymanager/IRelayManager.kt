package net.invictusmanagement.relaymanager

import net.invictusmanagement.relaymanager.models.RelayDeviceInfo
import kotlin.time.Duration


internal interface IRelayManager {
    val isInitialized: Boolean
    var delay: Duration
    val relayCount: Int

    suspend fun getDevices(): List<RelayDeviceInfo>

    suspend fun initializeDevice(id: String)
    suspend fun initializeDevice(device: RelayDeviceInfo)
    suspend fun openRelays(relayNumbers: List<Int>)
    suspend fun closeRelays(relayNumbers: List<Int>)
    suspend fun openAllRelays()
    suspend fun closeAllRelays()
    suspend fun isRelayOpen(relayNumber: Int): Boolean
    fun disconnect()
}