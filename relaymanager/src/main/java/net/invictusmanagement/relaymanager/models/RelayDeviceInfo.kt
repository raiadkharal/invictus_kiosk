package net.invictusmanagement.relaymanager.models

data class RelayDeviceInfo(
    val id: String,
    val name: String,
    val vendorId: Int,
    val productId: Int
)