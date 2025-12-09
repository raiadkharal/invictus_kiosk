package net.invictusmanagement.invictuskiosk.util.locale

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

val LocalAppLocale = staticCompositionLocalOf { Locale("en") }

object AppLocaleManager {
    val currentLocale = mutableStateOf(Locale("en"))
}
