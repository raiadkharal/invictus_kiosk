package net.invictusmanagement.invictuskiosk.commons

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Constants {
    const val BASE_URL = "https://kioskdev.invictusmanagement.net/api/v1/"
    const val  APP_PREF_KEY = "AppPrefs"
    const val SELECTED_LANGUAGE = "app_language"
    const val SELECTED_COUNTRY = "app_country"
    const val SCAN_RESULT = "scan_result"
    const val DIGITAL_KEY_GENERIC_ERROR = "We were unable to check this guest key at this time. Please try again later or contact your administrator."
    const val CONNECTION_ERROR = "Unable to connect to the server. Please check your internet connection and try again."

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDateString(
        inputDate: String,
        inputPattern: String = "yyyy-MM-dd'T'HH:mm:ss",
        outputPattern: String = "dd/MM/yyyy"
    ): String {
        return try {
            val inputFormatter = DateTimeFormatter.ofPattern(inputPattern)
            val outputFormatter = DateTimeFormatter.ofPattern(outputPattern)

            val dateTime = LocalDateTime.parse(inputDate, inputFormatter)
            outputFormatter.format(dateTime)
        } catch (e: Exception) {
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



}