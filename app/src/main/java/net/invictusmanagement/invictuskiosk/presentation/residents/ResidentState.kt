package net.invictusmanagement.invictuskiosk.presentation.residents

import net.invictusmanagement.invictuskiosk.domain.model.Resident

data class ResidentState(
    val isLoading: Boolean = false,
    val residents: List<Resident>? = emptyList(),
    val error: String = ""
)