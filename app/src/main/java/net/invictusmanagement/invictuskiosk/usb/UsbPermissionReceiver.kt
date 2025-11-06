package net.invictusmanagement.invictuskiosk.usb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log

class UsbPermissionReceiver(
    private val onGranted: (UsbDevice) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_USB_PERMISSION) {
            synchronized(this) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    device?.let(onGranted)
                } else {
                    Log.w("UsbPermissionReceiver", "USB permission denied for device ${device?.deviceName}")
                }
            }
        }
    }

    companion object {
        const val ACTION_USB_PERMISSION = "net.invictusmanagement.invictuskiosk.USB_PERMISSION"
    }
}
