package net.invictusmanagement.invictuskiosk.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.invictusmanagement.invictuskiosk.data.local.entities.ContactRequestEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.UnitImageEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.VacantUnitEntity

@Dao
interface VacanciesDao {

    @Query("SELECT * FROM vacant_units ORDER BY unitNbr ASC")
    suspend fun getUnits(): List<VacantUnitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<VacantUnitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactRequest(request: ContactRequestEntity)

    @Query("SELECT * FROM contact_requests")
    suspend fun getContactRequests(): List<ContactRequestEntity>

    @Query("SELECT * FROM unit_images WHERE unitImageId = :unitImageId")
    suspend fun getUnitImage(unitImageId: Long): UnitImageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnitImage(entity: UnitImageEntity)

    @Query("DELETE FROM unit_images")
    suspend fun clearUnitImages()

    @Query("DELETE FROM contact_requests WHERE localId = :localId")
    suspend fun deleteRequest(localId: Int)

    @Query("DELETE FROM vacant_units")
    suspend fun clearUnits()
}
