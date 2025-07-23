package net.invictusmanagement.invictuskiosk.domain.repository

import kotlinx.coroutines.flow.Flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.domain.model.AccessPoint
import net.invictusmanagement.invictuskiosk.domain.model.DigitalKey
import net.invictusmanagement.invictuskiosk.domain.model.Resident
import net.invictusmanagement.invictuskiosk.domain.model.Unit

interface ResidentsRepository {
    fun getResidentsByName(filter: String, byName: String): Flow<Resource<List<Resident>>>
    fun getResidentsByUnitNumber(unitNumber: String): Flow<Resource<List<Resident>>>
    fun validateDigitalKey(digitalKeyDto: DigitalKeyDto): Flow<Resource<DigitalKey>>
    fun getAllLeasingAgents(byName: String): Flow<Resource<List<Resident>>>
}