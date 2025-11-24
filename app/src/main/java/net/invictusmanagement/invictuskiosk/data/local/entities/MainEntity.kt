package net.invictusmanagement.invictuskiosk.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.invictusmanagement.invictuskiosk.data.remote.dto.home.Kiosk
import net.invictusmanagement.invictuskiosk.domain.model.home.Main

@Entity(tableName = "main_table")
data class MainEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val kiosk: Kiosk,
    val ssUrl: String
)

fun MainEntity.toMain(): Main {
    return Main(
        kiosk = kiosk,
        ssUrl = ssUrl
    )
}