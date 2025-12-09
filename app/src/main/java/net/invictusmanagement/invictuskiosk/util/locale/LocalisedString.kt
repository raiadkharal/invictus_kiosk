package net.invictusmanagement.invictuskiosk.util.locale

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun localizedString(id: Int): String {
    val locale = LocalAppLocale.current
    val context = LocalContext.current

    val config = context.resources.configuration.apply {
        setLocale(locale)
    }
    val localizedContext = context.createConfigurationContext(config)
    return localizedContext.getString(id)
}
