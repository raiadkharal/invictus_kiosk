package net.invictusmanagement.invictuskiosk.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import net.invictusmanagement.invictuskiosk.data.local.dao.DirectoryDao
import net.invictusmanagement.invictuskiosk.data.local.dao.HomeDao
import net.invictusmanagement.invictuskiosk.data.local.entities.IntroButtonEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.ResidentEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.UnitEntity

@Database(
    entities = [IntroButtonEntity::class, ResidentEntity::class, UnitEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun homeDao(): HomeDao
    abstract fun directoryDao(): DirectoryDao
}
