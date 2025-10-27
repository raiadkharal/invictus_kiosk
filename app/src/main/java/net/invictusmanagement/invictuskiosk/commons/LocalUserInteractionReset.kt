package net.invictusmanagement.invictuskiosk.commons

import androidx.compose.runtime.staticCompositionLocalOf

val LocalUserInteractionReset = staticCompositionLocalOf<(() -> Unit)?> { null }
