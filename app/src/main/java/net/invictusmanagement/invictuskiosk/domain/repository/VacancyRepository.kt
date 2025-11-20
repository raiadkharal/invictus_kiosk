package net.invictusmanagement.invictuskiosk.domain.repository

import kotlinx.coroutines.flow.Flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.domain.model.ContactRequest
import net.invictusmanagement.invictuskiosk.domain.model.Unit

interface VacancyRepository {
    fun getUnits(): Flow<Resource<List<Unit>>>
    fun sendContactRequest(contactRequest: ContactRequest): Flow<Resource<ContactRequest>>
    suspend fun syncPendingRequests()
}