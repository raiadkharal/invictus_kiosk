package net.invictusmanagement.invictuskiosk.data.local.converters

import androidx.room.TypeConverter

class LongListConverter {

    @TypeConverter
    fun fromList(list: List<Long>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toList(data: String?): List<Long>? {
        return data?.split(",")?.mapNotNull { it.toLongOrNull() }
    }
}
