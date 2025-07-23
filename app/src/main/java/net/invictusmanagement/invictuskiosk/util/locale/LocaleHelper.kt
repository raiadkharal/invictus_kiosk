package net.invictusmanagement.invictuskiosk.util.locale

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.core.content.edit
import net.invictusmanagement.invictuskiosk.commons.Constants
import java.util.Locale

// LocaleHelper.kt
object LocaleHelper {

    fun toggleLocale(context: Context) {
        val currentLocale = getCurrentLocale(context)
        val (newLanguage, newCountry) = if (currentLocale.language == "es") {
            "en" to "US"  // Switch to English
        } else {
            "es" to "MX"  // Switch to Spanish
        }

        updateLocale(context, newLanguage, newCountry)
    }

    private fun updateLocale(context: Context, language: String, country: String) {
        persistLocale(context, language, country)
        setAppLocale(context, language, country)
    }

    private fun persistLocale(context: Context, language: String, country: String) {
        context.getSharedPreferences(Constants.APP_PREF_KEY, Context.MODE_PRIVATE).edit {
            putString(Constants.SELECTED_LANGUAGE, language)
            putString(Constants.SELECTED_COUNTRY, country)
            apply()
        }
    }

    private fun setAppLocale(context: Context, language: String, country: String) {
        val locale = Locale(language, country)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }

    fun getCurrentLocale(context: Context): Locale {
        val prefs = context.getSharedPreferences(Constants.APP_PREF_KEY, Context.MODE_PRIVATE)
        val language = prefs.getString(Constants.SELECTED_LANGUAGE, "en") ?: "en"
        val country = prefs.getString(Constants.SELECTED_COUNTRY, "US") ?: "US"
        return Locale(language, country)
    }
}