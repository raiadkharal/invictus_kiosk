package net.invictusmanagement.invictuskiosk.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.invictusmanagement.invictuskiosk.data.local.converters.KioskConverter
import net.invictusmanagement.invictuskiosk.data.local.converters.LeasingOfficeConverter
import net.invictusmanagement.invictuskiosk.data.local.converters.LongListConverter
import net.invictusmanagement.invictuskiosk.data.local.converters.PromotionListConverter
import net.invictusmanagement.invictuskiosk.data.local.dao.CouponsDao
import net.invictusmanagement.invictuskiosk.data.local.dao.DirectoryDao
import net.invictusmanagement.invictuskiosk.data.local.dao.HomeDao
import net.invictusmanagement.invictuskiosk.data.local.dao.ResidentsDao
import net.invictusmanagement.invictuskiosk.data.local.dao.SystemLogDao
import net.invictusmanagement.invictuskiosk.data.local.dao.VacanciesDao
import net.invictusmanagement.invictuskiosk.data.local.entities.AccessPointEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.BusinessPromotionEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.ContactRequestEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.CouponsCategoryEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.IntroButtonEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.LeasingOfficeEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.MainEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.ResidentEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.SystemLogEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.UnitEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.UnitImageEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.VacantUnitEntity

@Database(
    entities = [
        IntroButtonEntity::class,
        ResidentEntity::class,
        UnitEntity::class,
        CouponsCategoryEntity::class,
        BusinessPromotionEntity::class,
        VacantUnitEntity::class,
        ContactRequestEntity::class,
        SystemLogEntity::class,
        MainEntity::class,
        LeasingOfficeEntity::class,
        AccessPointEntity::class,
        UnitImageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    PromotionListConverter::class,
    LongListConverter::class,
    KioskConverter::class,
    LeasingOfficeConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun homeDao(): HomeDao
    abstract fun directoryDao(): DirectoryDao
    abstract fun couponDao(): CouponsDao
    abstract fun vacanciesDao(): VacanciesDao
    abstract fun residentsDao(): ResidentsDao
    abstract fun systemLogDao(): SystemLogDao
}
