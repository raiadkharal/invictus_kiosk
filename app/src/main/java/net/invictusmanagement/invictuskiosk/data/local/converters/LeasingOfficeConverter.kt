package net.invictusmanagement.invictuskiosk.data.local.converters

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.invictusmanagement.invictuskiosk.data.remote.dto.LeasingOfficer

class LeasingOfficeConverter {

    @TypeConverter
    fun leasingOfficerToJson(value: LeasingOfficer?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun jsonToLeasingOfficer(json: String?): LeasingOfficer? {
        return json?.let { Json.decodeFromString<LeasingOfficer>(it) }
    }
}
