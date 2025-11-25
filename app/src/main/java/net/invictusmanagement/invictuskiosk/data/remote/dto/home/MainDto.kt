package net.invictusmanagement.invictuskiosk.data.remote.dto.home

import net.invictusmanagement.invictuskiosk.data.local.entities.MainEntity
import net.invictusmanagement.invictuskiosk.domain.model.home.Main

data class MainDto(
    val kiosk: Kiosk,
    val ssUrl: String?
)


fun MainDto.toMain(): Main {
    return Main(
        kiosk = kiosk,
        ssUrl = ssUrl ?: ""

    )
}

fun MainDto.toEntity(): MainEntity{
    return MainEntity(
        kiosk = kiosk,
        ssUrl = ssUrl ?: ""
    )
}