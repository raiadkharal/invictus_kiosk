package net.invictusmanagement.invictuskiosk.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.invictusmanagement.invictuskiosk.data.local.entities.IntroButtonEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.ResidentEntity

@Dao
interface HomeDao {

    @Query("SELECT * FROM intro_buttons")
    fun getButtons(): Flow<List<IntroButtonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertButtons(buttons: List<IntroButtonEntity>)

    @Query("SELECT * FROM residents")
    fun getAllResidents(): Flow<List<ResidentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResidents(list: List<ResidentEntity>)

    @Query("DELETE FROM residents")
    suspend fun clearResidents()

    @Query("DELETE FROM intro_buttons")
    suspend fun clearIntroButtons()
}
