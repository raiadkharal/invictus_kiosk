package net.invictusmanagement.invictuskiosk.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.invictusmanagement.invictuskiosk.data.local.entities.ContactRequestEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.VacantUnitEntity

@Dao
interface VacanciesDao {

    // Offline-first: observe unit list
    @Query("SELECT * FROM vacant_units ORDER BY unitNbr ASC")
    fun getUnits(): Flow<List<VacantUnitEntity>>

    // Insert or replace all units
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<VacantUnitEntity>)

    // Optional: get single unit by ID
    @Query("SELECT * FROM vacant_units WHERE id = :unitId LIMIT 1")
    fun getUnitById(unitId: Int): Flow<VacantUnitEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactRequest(request: ContactRequestEntity)

    @Query("SELECT * FROM contact_requests")
    fun getPendingRequests(): List<ContactRequestEntity>

    @Query("DELETE FROM contact_requests WHERE localId = :localId")
    suspend fun deleteRequest(localId: Int)

    @Query("DELETE FROM vacant_units")
    suspend fun clearUnits()
}
