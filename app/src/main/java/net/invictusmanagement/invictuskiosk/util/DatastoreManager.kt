package net.invictusmanagement.invictuskiosk.util

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.invictusmanagement.invictuskiosk.domain.model.AccessPoint
import net.invictusmanagement.invictuskiosk.domain.model.home.Main

// Create DataStore instance
private val Context.dataStore by preferencesDataStore(name = "app_preferences")

class DataStoreManager(private val context: Context) {

    // Keys
    companion object {
        private val ACCESS_POINT_KEY = stringPreferencesKey("access_point")
        private val KIOSK_DATA_KEY = stringPreferencesKey("kiosk_data")
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val ACTIVATION_CODE_KEY = stringPreferencesKey("activation_code")
    }

    private val json = Json { ignoreUnknownKeys = true }

    // Save custom object
    suspend fun saveAccessPoint(accessPoint: AccessPoint?) {
        val jsonString = json.encodeToString(accessPoint)
        context.dataStore.edit { prefs ->
            prefs[ACCESS_POINT_KEY] = jsonString
        }
    }

    // Read custom object
    val accessPointFlow: Flow<AccessPoint?> = context.dataStore.data.map { prefs ->
        prefs[ACCESS_POINT_KEY]?.let { jsonString ->
            try {
                json.decodeFromString<AccessPoint>(jsonString)
            } catch (e: Exception) {
                null
            }
        }
    }

    // Save custom object
    suspend fun saveKioskData(kioskData: Main?) {
        val jsonString = json.encodeToString(kioskData)
        context.dataStore.edit { prefs ->
            prefs[KIOSK_DATA_KEY] = jsonString
        }
    }

    // Read custom object
    val kioskDataFlow: Flow<Main?> = context.dataStore.data.map { prefs ->
        prefs[KIOSK_DATA_KEY]?.let { jsonString ->
            try {
                json.decodeFromString<Main>(jsonString)
            } catch (e: Exception) {
                null
            }
        }
    }

    // Save access token
    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = token
        }
    }

    // Read String
    val accessTokenFlow: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[ACCESS_TOKEN_KEY] ?: "" }

    // Save access token
    suspend fun saveActivationCode(activationCode: String) {
        context.dataStore.edit { prefs ->
            prefs[ACTIVATION_CODE_KEY] = activationCode
        }
    }

    // Read String
    val activationCodeFlow: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[ACTIVATION_CODE_KEY] }

//    // Save Boolean
//    suspend fun setLoggedIn(isLoggedIn: Boolean) {
//        context.dataStore.edit { prefs ->
//            prefs[LOGGED_IN_KEY] = isLoggedIn
//        }
//    }
//
//    // Read Boolean
//    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data
//        .map { prefs -> prefs[LOGGED_IN_KEY] ?: false }
//
//    // Save Int
//    suspend fun saveUserAge(age: Int) {
//        context.dataStore.edit { prefs ->
//            prefs[USER_AGE_KEY] = age
//        }
//    }
//
//    // Read Int
//    val userAgeFlow: Flow<Int?> = context.dataStore.data
//        .map { prefs -> prefs[USER_AGE_KEY] }

    // Clear all preferences
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
