package net.invictusmanagement.invictuskiosk.commons

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object Constants {
    const val  APP_PREF_KEY = "AppPrefs"
    const val SELECTED_LANGUAGE = "app_language"
    const val SELECTED_COUNTRY = "app_country"
    const val SCAN_RESULT = "scan_result"
    const val DIGITAL_KEY_GENERIC_ERROR = "We were unable to check this guest key at this time. Please try again later or contact your administrator."
    const val QR_CODE_GENERIC_ERROR = "We were unable to check this QR code at this time. Please try again later or contact your administrator."
    const val SERVICE_KEY_GENERIC_ERROR = "We were unable to check this service key at this time. Please try again later or contact your administrator."
    const val VIDEO_MAIL_GENERIC_ERROR = "We were unable to process your voicemail at this time. Please try again later or contact your administrator."
    const val CONNECTION_ERROR = "Unable to connect to the server. Please check your internet connection and try again."
    const val UPLOAD_SUCCESS_MESSAGE = "Your voicemail has been successfully uploaded."


    fun formatDateString(
        inputDate: String,
        inputPattern: String = "yyyy-MM-dd'T'HH:mm:ss",
        outputPattern: String = "dd/MM/yyyy"
    ): String {
        return try {
            val inputFormatter = DateTimeFormatter.ofPattern(inputPattern)
                .withZone(ZoneOffset.UTC) // interpret input as UTC

            val outputFormatter = DateTimeFormatter.ofPattern(outputPattern)
                .withZone(ZoneId.systemDefault()) // convert to device local zone

            // Parse as Instant
            val instant = LocalDateTime.parse(inputDate, inputFormatter)
                .atOffset(ZoneOffset.UTC)
                .toInstant()

            outputFormatter.format(instant)
        } catch (e: Exception) {
            Log.d("formatDateString", "Error: ${e.message}")
            ""
        }
    }


    fun formatNumber(value: Float): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString() // remove .0
        } else {
            value.toString() // keep decimal part
        }
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val digitsOnly = phoneNumber.filter { it.isDigit() }
        return digitsOnly.length == 10
    }

    fun formatPhoneNumber(input: String): String {
        val digits = input.filter { it.isDigit() }
        return if (digits.length == 10) {
            "(${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6)}"
        } else {
            digits
        }
    }

    fun getFriendlyCameraError(e: Exception): String = when (e) {
        is java.util.concurrent.ExecutionException -> "Camera failed to start. Please try again."
        is IllegalStateException -> "Camera is not available right now."
        is SecurityException -> "Camera permission is missing. Please enable it in settings."
        else -> "Unable to initialize the camera. Please try again."
    }

    fun isAnyMicAvailable(context: Context): Boolean {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                ?: return false  // Fail safely if AudioManager is null

            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
                ?: return false  // Just in case OEM returns null

            devices.any { device ->
                when (device.type) {
                    AudioDeviceInfo.TYPE_BUILTIN_MIC,
                    AudioDeviceInfo.TYPE_USB_DEVICE,
                    AudioDeviceInfo.TYPE_USB_HEADSET,
                    AudioDeviceInfo.TYPE_USB_ACCESSORY,
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> true
                    else -> false
                }
            }
        } catch (e: Exception) {
            Log.d("isAnyMicAvailable", "Error checking microphone availability: ${e.message}")
            false
        }
    }


}