package net.invictusmanagement.invictuskiosk.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.invictusmanagement.invictuskiosk.data.local.entities.ResidentEntity

@Dao
interface ResidentsDao {

    @Query(
        """
    SELECT * FROM residents
    WHERE CASE 
        WHEN :byName = 'l' 
            THEN REPLACE(displayName, ' ', '_') LIKE '%' || '_' || :filter || '%'
        ELSE displayName LIKE :filter || '%'
    END
"""
    )
    suspend fun getResidentsByName(filter: String, byName: String): List<ResidentEntity>

    @Query(
        """
    SELECT * FROM residents
    WHERE unitId = :unitId
        AND role IN (:allowedRoles)
    ORDER BY role, displayName
"""
    )
    suspend fun getResidentsByUnit(
        unitId: Int?,
        allowedRoles: List<String> = listOf("Resident", "Administrator", "LeasingAgent")
    ): List<ResidentEntity>


    @Query(
        """
    SELECT * FROM residents
    WHERE role NOT IN (:excludedRoles)
    ORDER BY role, displayName
"""
    )
    suspend fun getAllLeasingAgents(
        excludedRoles: List<String> = listOf(
            "Resident",
            "Guest",
            "Prospect",
            "Headquarters",
            "Administrator"
        )
    ): List<ResidentEntity>
}
