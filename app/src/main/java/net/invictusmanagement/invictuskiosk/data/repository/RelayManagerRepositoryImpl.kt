package net.invictusmanagement.invictuskiosk.data.repository

import net.invictusmanagement.invictuskiosk.domain.repository.RelayManagerRepository
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import net.invictusmanagement.relaymanager.RelayManager
import net.invictusmanagement.relaymanager.models.OpenRelayModel

class RelayManagerRepositoryImpl(
    private val relayManager: RelayManager,
    private val logger: GlobalLogger
) : RelayManagerRepository {
    override var relayId: String? = null

    override suspend fun initializeRelayManager() {
        try {
            val devices = relayManager.getDevices()
            logger.log("initializeRelayManager", "Found ${devices.size} relay devices.")
            if(devices.isNotEmpty()) {
                relayId = devices.first().id
                logger.log("initializeRelayManager", "Relay manager connection established to: $relayId")
            }else{
                logger.log("initializeRelayManager", "No USB Relay Devices are detected")
            }
        }catch (e: Exception){
            logger.log("initializeRelayManager", "Unable to communicate with relay device",e)
        }
    }

    override suspend fun openAccessPoint(
        relayPort: Int?,
        relayOpenTimer: Int?,
        relayDelayTimer: Int?
    ): Result<Unit> {
        if (relayId.isNullOrEmpty()) {
            logger.log("openAccessPoint", "Relay manager identifier is missing.")
            return Result.failure(IllegalStateException("Relay manager identifier is missing."))
        }

        val relayPort: Int = relayPort ?: 0
        val relayOpenTimer: Int = relayOpenTimer ?: 0
        val relayDelayTimer: Int = relayDelayTimer ?: 0

        logger.log("openAccessPoint", "Opening access point: id=$relayId port=$relayPort open=$relayOpenTimer delay=$relayDelayTimer")

        val model = OpenRelayModel(
            relayId = relayId ?: "",
            relayNumber = relayPort,
            relayOpenTimer = relayOpenTimer,
            relayDelayTimer = relayDelayTimer
        )

        return try {
            logger.log("openAccessPoint", "Invictus: Going to Open Door - ${model.relayId}:${model.relayNumber}:${model.relayDelayTimer}")
            val result = relayManager.open(model)

            result.onSuccess {
                logger.log("openAccessPoint", "Access point opened successfully for relay ${model.relayNumber}")
            }.onFailure { exception ->
                logger.log("openAccessPoint", "Failed to open access point: ${exception.message}", exception)
            }

            result
        } catch (e: Exception) {
            logger.log("openAccessPoint", "Unable to open access point. ${e.message}", e)
            Result.failure(e)
        }
    }

}