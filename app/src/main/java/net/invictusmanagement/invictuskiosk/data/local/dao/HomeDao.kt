package net.invictusmanagement.invictuskiosk.data.local.dao

import androidx.room.*
import net.invictusmanagement.invictuskiosk.data.local.entities.AccessPointEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.IntroButtonEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.LeasingOfficeEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.MainEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.ResidentEntity

@Dao
interface HomeDao {

    @Query("SELECT * FROM intro_buttons")
    suspend fun getButtons(): List<IntroButtonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertButtons(buttons: List<IntroButtonEntity>)

    @Query("SELECT * FROM residents")
    suspend fun getAllResidents(): List<ResidentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResidents(list: List<ResidentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccessPoints(list: List<AccessPointEntity>)

    @Query("SELECT * FROM access_points")
    suspend fun getAccessPoints(): List<AccessPointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKioskData(mainEntity: MainEntity)

    @Query("SELECT * FROM main_table LIMIT 1")
    suspend fun getKioskData(): MainEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeasingOfficeDetail(leasingOffice: LeasingOfficeEntity)

    @Query("SELECT * FROM leasing_office LIMIT 1")
    suspend fun getLeasingOfficeDetail(): LeasingOfficeEntity?

    @Query("DELETE FROM leasing_office")
    suspend fun clearLeasingOfficeDetail()

    @Query("DELETE FROM main_table")
    suspend fun clearKioskData()

    @Query("DELETE FROM access_points")
    suspend fun clearAllAccessPoints()

    @Query("DELETE FROM residents")
    suspend fun clearResidents()

    @Query("DELETE FROM intro_buttons")
    suspend fun clearIntroButtons()
}
