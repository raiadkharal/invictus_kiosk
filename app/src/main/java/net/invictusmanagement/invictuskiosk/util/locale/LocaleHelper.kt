package net.invictusmanagement.invictuskiosk.util.locale

import android.content.Context
import androidx.core.content.edit
import net.invictusmanagement.invictuskiosk.commons.Constants
import java.util.Locale

object LocaleHelper {

    fun toggleLocale(context: Context): Locale {
        val current = getCurrentLocale(context)
        val newLocale =
            if (current.language == "en") Locale("es") else Locale("en")

        saveLocale(context, newLocale)
        return newLocale
    }

    fun getCurrentLocale(context: Context): Locale {
        val prefs = context.getSharedPreferences(Constants.APP_PREF_KEY, Context.MODE_PRIVATE)
        val lang = prefs.getString(Constants.SELECTED_LANGUAGE, "en") ?: "en"
        return Locale(lang)
    }

    private fun saveLocale(context: Context, locale: Locale) {
        context.getSharedPreferences(Constants.APP_PREF_KEY, Context.MODE_PRIVATE)
            .edit {
                putString(Constants.SELECTED_LANGUAGE, locale.language)
            }
    }
}
