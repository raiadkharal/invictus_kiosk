package net.invictusmanagement.relaymanager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import net.invictusmanagement.relaymanager.models.RelayDeviceInfo
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import java.util.concurrent.atomic.AtomicBoolean

internal class NumatoRelayManager(private val context: Context) : IRelayManager {

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
                    Log.e(TAG, "Command execution failed", e)
                }
            }
        }
    }

    override suspend fun getDevices(): List<RelayDeviceInfo> = withContext(Dispatchers.IO) {
        val devices = mutableListOf<RelayDeviceInfo>()
        val deviceList = usbManager.deviceList.values

        if (deviceList.isEmpty()) {
            Log.i("RelayManager", "No USB relay devices found.")
            return@withContext emptyList()
        }

        for (device in deviceList) {
            try {
                // Filter: Only include Numato devices (vendorId = 0x2A19)
                if (device.vendorId == 0x2A19) {
                    val info = RelayDeviceInfo(
                        id = device.serialNumber ?: device.deviceName,
                        name = device.productName ?: "Numato USB Relay",
                        vendorId = device.vendorId,
                        productId = device.productId
                    )
                    devices.add(info)
                }
            } catch (e: Exception) {
                Log.e("RelayManager", "Error reading device info: ${e.message}")
            }
        }

        Log.i("RelayManager", "Found ${devices.size} relay devices.")
        return@withContext devices
    }


    override suspend fun initializeDevice(id: String) {
        initialized.set(false)

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
            Log.i(TAG, "Requesting permission for ${usbDevice.deviceName}")
            return
        }

        // --- Equivalent to: _controller.OpenBySerialNumber(id)
        val driver = UsbSerialProber.getDefaultProber().probeDevice(usbDevice)
            ?: throw IllegalStateException("No serial driver found for ${usbDevice.deviceName}")

        val connection = usbManager.openDevice(driver.device)
            ?: throw IllegalStateException("Failed to open USB connection")

        // --- Equivalent to: SetBitMode (configure serial parameters)
        port = driver.ports.firstOrNull()
        port?.open(connection)
        port?.setParameters(
            9600,
            8,
            UsbSerialPort.STOPBITS_1,
            UsbSerialPort.PARITY_NONE
        )

        // Successfully initialized
        initialized.set(true)
        Log.i(TAG, "Relay device initialized successfully on port ${port?.portNumber}")
    }

    override suspend fun initializeDevice(device: RelayDeviceInfo) {
        initializeDevice(device.id)
    }


    override suspend fun openRelays(relayNumbers: List<Int>) = enqueue {
        if (!initialized.get()) {
            throw IllegalStateException("No device initialized.")
        }
        relayNumbers.forEach { relayNumber ->
            if (relayNumber in 0..relayCount) {
                sendCommand("relay on $relayNumber\r")
            }
        }
    }

    override suspend fun closeRelays(relayNumbers: List<Int>) = enqueue {
        if (!initialized.get()) {
            throw IllegalStateException("No device initialized.")
        }

        relayNumbers.forEach { relayNumber ->
            if (relayNumber in 0..relayCount) {
                sendCommand("relay off $relayNumber\r")
            }
        }
    }

    override suspend fun openAllRelays() = enqueue {
        if (!initialized.get()) {
            throw IllegalStateException("No device initialized.")
        }

        sendCommand("relay writeall 0F\r")
    }

    override suspend fun closeAllRelays() = enqueue {
        if (!initialized.get()) {
            throw IllegalStateException("No device initialized.")
        }

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
                Log.i("RelayManager", "Relay device disconnected.")
            } catch (e: Exception) {
                Log.e("RelayManager", "Error closing relay connection", e)
            }
        }
    }

    private suspend fun enqueue(block: suspend () -> Unit) {
        commandQueue.send(block)
    }

    private suspend fun sendCommand(cmd: String) {
        port?.write(cmd.toByteArray(), 500)
        delay(delay)
    }

    private fun readResponse(): String? {
        val buffer = ByteArray(128)
        val len = port?.read(buffer, 500) ?: 0
        return if (len > 0) String(buffer, 0, len).trim() else null
    }

    companion object {
        private const val TAG = "RelayManagerImpl"
        const val ACTION_USB_PERMISSION = "com.invictus.RELAY_USB_PERMISSION"

        @Volatile
        private var instance: NumatoRelayManager? = null

        fun getInstance(context: Context): NumatoRelayManager {
            return instance ?: synchronized(this) {
                instance ?: NumatoRelayManager(context.applicationContext).also { instance = it }
            }
        }
    }
}