package net.invictusmanagement.invictuskiosk.presentation.vacancy

import net.invictusmanagement.invictuskiosk.domain.model.Unit

data class VacancyState(
    val isLoading: Boolean = false,
    val vacancies: List<Unit> = emptyList(),
    val error: String = ""

)
