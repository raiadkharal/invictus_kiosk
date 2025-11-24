package net.invictusmanagement.invictuskiosk.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.invictusmanagement.invictuskiosk.data.local.entities.UnitEntity

@Dao
interface DirectoryDao {

    @Query("SELECT * FROM units ORDER BY unitNbr")
    suspend fun getUnits(): List<UnitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(list: List<UnitEntity>)

    @Query("SELECT * FROM units WHERE unitNbr = :number")
    suspend fun getUnitByNumber(number: String): UnitEntity?

    @Query("DELETE FROM units")
    suspend fun clearUnits()
}
