package net.invictusmanagement.relaymanager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import net.invictusmanagement.relaymanager.models.RelayDeviceInfo
import net.invictusmanagement.relaymanager.util.ILogger
import org.json.JSONArray
import org.json.JSONObject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.log

internal class NumatoRelayManager(
    private val context: Context,
    private val logger: ILogger
) : IRelayManager {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var port: UsbSerialPort? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val commandQueue = Channel<suspend () -> Unit>(Channel.UNLIMITED)
    private val initialized = AtomicBoolean(false)

    override val isInitialized: Boolean
        get() = initialized.get()

    override var delay: Duration =100.milliseconds
    override val relayCount: Int = 4

    init {
        // Launch background command processor
        scope.launch {
            for (cmd in commandQueue) {
                try {
                    cmd()
                } catch (e: Exception) {
                    logger.log("NumatoRelayManager", "Command execution failed", e)
                }
            }
        }
    }

    override suspend fun getDevices(): List<RelayDeviceInfo> = withContext(Dispatchers.IO) {
        val devices = mutableListOf<RelayDeviceInfo>()
        val deviceList = usbManager.deviceList.values

        logger.log("getDevices", "Found ${deviceList.size} devices.")
        logger.log("getDevices", "Devices: $deviceList")
        logUsbDevices(deviceList,logger)

        if (deviceList.isEmpty()) {
            return@withContext emptyList()
        }

        for (device in deviceList) {
            try {
                // Filter: Only include Numato devices (vendorId = 0x2A19)
                if (device.vendorId == 0x2A19) {
                    val info = RelayDeviceInfo(
                        id = device.deviceName,
                        name = device.productName ?: "Numato USB Relay",
                        vendorId = device.vendorId,
                        productId = device.productId
                    )
                    devices.add(info)
                }
            } catch (e: Exception) {
                throw IllegalStateException("Error reading device info: ${e.message}", e)
            }
        }

        return@withContext devices
    }


    override suspend fun initializeDevice(id: String) {
        initialized.set(false)

        logger.log("initializeDevice", "Initializing device with ID: $id")

        val device = usbManager.deviceList.values.firstOrNull { it.deviceName == id }
            ?: throw IllegalArgumentException("Device with ID $id not found")

        val usbDevice = device
        val permissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE
        )

        if (!usbManager.hasPermission(usbDevice)) {
            usbManager.requestPermission(usbDevice, permissionIntent)
            logger.log("initializeDevice", "Requesting permission for ${usbDevice.deviceName}", null)
            return
        }
        logger.log("initializeDevice", "Permission granted for ${usbDevice.deviceName}")

        // --- Equivalent to: _controller.OpenBySerialNumber(id)
        val driver = UsbSerialProber.getDefaultProber().probeDevice(usbDevice)
            ?: throw IllegalStateException("No serial driver found for ${usbDevice.deviceName}")

        logger.log("initializeDevice", "Driver found: ${driver.javaClass.simpleName}")

        val connection = usbManager.openDevice(driver.device)
            ?: throw IllegalStateException("Failed to open USB connection")

        logger.log("initializeDevice", "Connection opened successfully")

        // --- Equivalent to: SetBitMode (configure serial parameters)
        port = driver.ports.firstOrNull()

        logger.log("initializeDevice", "Port found: ${port?.portNumber}")

        port?.open(connection)
        port?.setParameters(
            9600,
            8,
            UsbSerialPort.STOPBITS_1,
            UsbSerialPort.PARITY_NONE
        )

        // Successfully initialized
        initialized.set(true)
        logger.log("initializeDevice", "Relay device initialized successfully on port ${port?.portNumber}")
    }

    override suspend fun initializeDevice(device: RelayDeviceInfo) {
        initializeDevice(device.id)
    }


    override suspend fun openRelays(relayNumbers: List<Int>) = enqueue {
        if (!initialized.get()) {
            throw IllegalStateException("No device initialized.")
        }

        logger.log("openRelays", "Opening relays: $relayNumbers")
        relayNumbers.forEach { relayNumber ->
            if (relayNumber in 0 until relayCount) {
                sendCommand("relay on $relayNumber\r")
            }
        }
    }

    override suspend fun closeRelays(relayNumbers: List<Int>) = enqueue {
        if (!initialized.get()) {
            throw IllegalStateException("No device initialized.")
        }

        logger.log("closeRelays", "Closing relays: $relayNumbers")
        relayNumbers.forEach { relayNumber ->
            if (relayNumber in 0 until relayCount) {
                sendCommand("relay off $relayNumber\r")
            }
        }
    }

    override suspend fun openAllRelays() = enqueue {
        if (!initialized.get()) {
            throw IllegalStateException("No device initialized.")
        }

        logger.log("openAllRelays", "Opening all relays")
        sendCommand("relay writeall 0F\r")
    }

    override suspend fun closeAllRelays() = enqueue {
        if (!initialized.get()) {
            throw IllegalStateException("No device initialized.")
        }

        logger.log("closeAllRelays", "Closing all relays")
        sendCommand("relay writeall 00\r")
    }

    override suspend fun isRelayOpen(relayNumber: Int): Boolean {
        val response = withContext(Dispatchers.IO) {
            sendCommand("relay read $relayNumber\r")
            delay(delay)
            readResponse()
        }
        return response?.contains("on", ignoreCase = true) == true
    }

    override fun disconnect() {
        scope.launch(Dispatchers.IO) {
            try {
                port?.close()
                job.cancel()
                initialized.set(false)
                logger.log("disconnect", "Relay device disconnected")
            } catch (e: Exception) {
                logger.log("disconnect", "Error closing relay connection", e)
            }
        }
    }

    private suspend fun enqueue(block: suspend () -> Unit) {
        logger.log("enqueue", "Enqueuing command")
        commandQueue.send(block)
    }

    private suspend fun sendCommand(cmd: String) {
        logger.log("sendCommand", "Sending command: $cmd")
        port?.write(cmd.toByteArray(), 500)
        delay(delay)
    }

    private fun readResponse(): String? {
        val buffer = ByteArray(128)
        val len = port?.read(buffer, 500) ?: 0
        return if (len > 0) String(buffer, 0, len).trim() else null
    }

    companion object {
        const val ACTION_USB_PERMISSION = "net.invictusmanagement.invictuskiosk.USB_PERMISSION"

        @Volatile
        private var instance: NumatoRelayManager? = null

        fun getInstance(context: Context, logger: ILogger): NumatoRelayManager {
            return instance ?: synchronized(this) {
                instance ?: NumatoRelayManager(context.applicationContext, logger).also { instance = it }
            }
        }
    }


    fun logUsbDevices(deviceList: Collection<UsbDevice>, logger: ILogger) {
        val jsonArray = JSONArray()

        for (device in deviceList) {
            try {
                val jsonDevice = JSONObject().apply {
                    put("deviceId", device.deviceId)
                    put("name", device.deviceName)
                    put("vendorId", device.vendorId)
                    put("productId", device.productId)
                    put("deviceClass", device.deviceClass)
                    put("deviceProtocol", device.deviceProtocol)

                    // Safely add optional properties
                    put("serialNumber", runCatching { device.serialNumber }.getOrNull())
                    put("manufacturerName", runCatching { device.manufacturerName }.getOrNull())
                    put("productName", runCatching { device.productName }.getOrNull())
                    put("version", runCatching { device.version }.getOrNull())
                }
                jsonArray.put(jsonDevice)
            } catch (e: Exception) {
                logger.log("getDevices", "Error reading device info: ${e.message}")
            }
        }

        try {
            val jsonString = JSONObject().put("devices", jsonArray).toString(2)
            logger.log("getDevices DeviceList", jsonString)
        } catch (e: Exception) {
            logger.log("getDevices", "Failed to build JSON: ${e.message}")
        }
    }

}