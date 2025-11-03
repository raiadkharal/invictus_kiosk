package net.invictusmanagement.relaymanager

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.invictusmanagement.relaymanager.models.OpenRelayModel
import net.invictusmanagement.relaymanager.models.RelayDeviceInfo
import java.io.File
import java.nio.file.Paths

class RelayManager(
    context: Context
) {

    private var relayManager: IRelayManager = NumatoRelayManager(context)

    private val tag = "InvictusRelayManager"


    suspend fun initializeDevice(id: String){
        relayManager.initializeDevice(id)
    }


    suspend fun getDevices(): List<RelayDeviceInfo> = withContext(Dispatchers.IO) {
        return@withContext try {
//            val devices = listOf(
//                RelayDeviceInfo(
//                    id = "AE017Q9D",
//                    name = "FT245R USB FIFO",
//                    vendorId = 0,
//                    productId = 0,
//                )
//            )
//            Log.i(tag, "Devices fetched successfully: $devices")
//            devices
             relayManager.getDevices()
        } catch (ex: Exception) {
            Log.e(tag, "Error getting devices: ${ex.message}", ex)
            emptyList()
        }
    }

    suspend fun open(model: OpenRelayModel): Result<Unit> {
        Log.i(tag, "Invictus: Going to Open Door - ${model.relayId}:${model.relayNumber}:${model.relayDelayTimer}")

        return try {
            if (!relayManager.isInitialized) {
                relayManager.initializeDevice(model.relayId)
            }

            val relays = listOf(model.relayNumber)

            if (model.relayDelayTimer > 0) {
                delay(model.relayDelayTimer.toLong())
            }

            relayManager.openRelays(relays)

            if (model.relayOpenTimer > 0) {
                delay(model.relayOpenTimer.toLong())
            }

            relayManager.closeRelays(relays)

            Log.i(tag, "Invictus: Opened - ${model.relayId}:${model.relayNumber}:${model.relayDelayTimer}")
            Result.success(Unit)
        } catch (ex: Exception) {
            Log.e(tag, "Invictus: Error in Open Method - ${ex.message}", ex)
//            restartServiceItself()
            Result.failure(ex)
        }
    }

    suspend fun checkRelay(pwd: String?, relayId: String, port: Int = 3): Boolean {
        return try {
            if (!pwd.isNullOrBlank() && pwd.trim().lowercase() == "tatva123") {
                if (!relayManager.isInitialized) {
                    relayManager.initializeDevice(relayId)
                }

                val relays = listOf(port)
                if (!relayManager.isRelayOpen(port)) {
                    relayManager.openRelays(relays)
                    relayManager.closeRelays(relays)
                }

                true
            } else {
               false
            }
        } catch (ex: Exception) {
            Log.e(tag, "Invictus: Error in CheckRelay Method ${ex.message}", ex)
//            restartServiceItself()
            false
        }
    }

    fun getKioskCode(): String {
        return try {
            val assemblyLocation = File(RelayManager::class.java.protectionDomain.codeSource.location.toURI()).parent
            val path = Paths.get(assemblyLocation, "Settings", "KioskCode.txt").toFile()

            if (path.exists()) {
                val kioskCode = path.readText().trim()
                kioskCode
            } else {
               ""
            }
        } catch (ex: Exception) {
            ""
        }
    }


}
