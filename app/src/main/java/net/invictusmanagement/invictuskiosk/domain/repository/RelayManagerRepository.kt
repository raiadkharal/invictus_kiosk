package net.invictusmanagement.invictuskiosk.domain.repository

interface RelayManagerRepository {
    var relayId:String?
    suspend fun initializeRelayManager()
    suspend fun openAccessPoint(relayPort: Int?, relayOpenTimer: Int?, relayDelayTimer: Int?): Result<Unit>
}