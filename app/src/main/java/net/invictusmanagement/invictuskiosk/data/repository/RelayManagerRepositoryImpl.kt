package net.invictusmanagement.invictuskiosk.data.repository

import android.util.Log
import net.invictusmanagement.invictuskiosk.domain.repository.RelayManagerRepository
import net.invictusmanagement.relaymanager.RelayManager
import net.invictusmanagement.relaymanager.models.OpenRelayModel

class RelayManagerRepositoryImpl(
    private val relayManager: RelayManager
) : RelayManagerRepository {
    override var relayId: String? = null
    private val tag = "relayManagerRepository"

    override suspend fun initializeRelayManager() {
        try {
            val devices = relayManager.getDevices()
            if(devices.isNotEmpty()) {
                relayId = devices.first().id
                Log.d(tag, "initializeRelayManager: Relay manager connection established to: $relayId")
            }else{
                Log.d(tag, "initializeRelayManager: No Relay Devices are detected")
            }
        }catch (e: Exception){
            Log.d(tag, "initializeRelayManager: ${e.message} - Unable to communicate with relay device")
        }
    }

    override suspend fun openAccessPoint(
        relayPort: Int?,
        relayOpenTimer: Int?,
        relayDelayTimer: Int?
    ): Result<Unit> {
        if (relayId.isNullOrEmpty()) {
            Log.w(tag, "Relay manager identifier is missing.")
            return Result.failure(IllegalStateException("Relay manager identifier is missing."))
        }

        val relayPort: Int = relayPort ?: 0
        val relayOpenTimer: Int = relayOpenTimer ?: 0
        val relayDelayTimer: Int = relayDelayTimer ?: 0

        Log.d(tag, "Opening access point: id=$relayId port=$relayPort open=$relayOpenTimer delay=$relayDelayTimer")

        val model = OpenRelayModel(
            relayId = relayId ?: "",
            relayNumber = relayPort,
            relayOpenTimer = relayOpenTimer,
            relayDelayTimer = relayDelayTimer
        )

        return try {
            val result = relayManager.open(model)

            result.onSuccess {
                Log.i(tag, "Access point opened successfully for relay ${model.relayNumber}")
            }.onFailure { exception ->
                Log.e(tag, "Failed to open access point: ${exception.message}", exception)
            }

            result
        } catch (e: Exception) {
            Log.e(tag, "Unable to open access point. ${e.message}", e)
            Result.failure(e)
        }
    }

}