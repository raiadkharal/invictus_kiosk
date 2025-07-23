package net.invictusmanagement.invictuskiosk.data.remote.dto.home

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