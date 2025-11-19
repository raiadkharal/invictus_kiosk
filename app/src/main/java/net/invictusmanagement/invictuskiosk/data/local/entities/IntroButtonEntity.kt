package net.invictusmanagement.invictuskiosk.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intro_buttons")
data class IntroButtonEntity(
    @PrimaryKey(autoGenerate = false)
    val name: String
)
