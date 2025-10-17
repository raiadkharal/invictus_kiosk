package net.invictusmanagement.invictuskiosk.domain.repository

import kotlinx.coroutines.flow.Flow
import net.invictusmanagement.invictuskiosk.commons.Resource

interface UnitMapRepository {
    fun getMapImage(unitId: Long, unitMapId: Long, toPackageCenter: Boolean = false): Flow<Resource<ByteArray>>
    fun getUnitImage(unitId: Long, unitImageId: Long): Flow<Resource<ByteArray>>
}