package net.invictusmanagement.invictuskiosk.domain.repository

import kotlinx.coroutines.flow.Flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.data.repository.Repository
import net.invictusmanagement.invictuskiosk.domain.model.home.Main
import net.invictusmanagement.invictuskiosk.domain.model.AccessPoint
import net.invictusmanagement.invictuskiosk.domain.model.DigitalKey
import net.invictusmanagement.invictuskiosk.domain.model.LeasingOffice
import net.invictusmanagement.invictuskiosk.domain.model.Resident

interface HomeRepository: Repository {

     fun validateDigitalKey(digitalKeyDto: DigitalKeyDto): Flow<Resource<DigitalKey>>

     fun getAccessPoints(): Flow<Resource<List<AccessPoint>>>

     fun getAllResidents(): Flow<Resource<List<Resident>>>

     fun getKioskData(): Flow<Resource<Main>>

     fun getLeasingOfficeDetails(): Flow<Resource<LeasingOffice>>

     fun getIntroButtons(): Flow<Resource<List<String>>>
}