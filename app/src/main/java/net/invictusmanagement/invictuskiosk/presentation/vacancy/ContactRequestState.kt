package net.invictusmanagement.invictuskiosk.presentation.vacancy

import net.invictusmanagement.invictuskiosk.domain.model.ContactRequest
import net.invictusmanagement.invictuskiosk.domain.model.Unit

data class ContactRequestState(
    val isLoading: Boolean = false,
    val contactRequest: ContactRequest? = null,
    val error: String = ""

)
