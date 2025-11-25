package net.invictusmanagement.invictuskiosk.data.local.converters

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.invictusmanagement.invictuskiosk.domain.model.Promotion

object PromotionListConverter {

    @TypeConverter
    fun fromList(list: List<Promotion>): String =
        Json.encodeToString(list)

    @TypeConverter
    fun toList(data: String): List<Promotion> =
        Json.decodeFromString(data)
}
