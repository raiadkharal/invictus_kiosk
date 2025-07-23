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
    const val ROOM_NAME = "room123"

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

}