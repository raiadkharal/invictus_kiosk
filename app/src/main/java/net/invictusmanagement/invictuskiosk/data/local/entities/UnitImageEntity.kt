package net.invictusmanagement.invictuskiosk.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unit_images")
data class UnitImageEntity(
    @PrimaryKey(autoGenerate = false)
    val unitImageId: Long,
    val unitId: Int,
    val imagePath: String
)
