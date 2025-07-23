package net.invictusmanagement.invictuskiosk.presentation.service_key

import net.invictusmanagement.invictuskiosk.domain.model.DigitalKey
import net.invictusmanagement.invictuskiosk.domain.model.Login
import net.invictusmanagement.invictuskiosk.domain.model.ServiceKey

data class ServiceKeyState(
    val isLoading: Boolean = false,
    val digitalKey: ServiceKey? = null,
    val error: String = ""
)