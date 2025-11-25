package net.invictusmanagement.invictuskiosk.data.local.converters

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.invictusmanagement.invictuskiosk.data.remote.dto.home.Kiosk

class KioskConverter {

    @TypeConverter
    fun fromKiosk(kiosk: Kiosk): String {
        return Json.encodeToString(kiosk)
    }

    @TypeConverter
    fun toKiosk(json: String): Kiosk {
        return Json.decodeFromString(json)
    }
}
