package net.invictusmanagement.invictuskiosk.util.locale

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

// AppLocale.kt
object AppLocale {
    private var currentLocale: Locale by mutableStateOf(Locale.ENGLISH)

    fun updateLocale(newLocale: Locale) {
        currentLocale = newLocale
    }
}