package net.invictusmanagement.invictuskiosk.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val migration3to4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS unit_images (
                unitImageId INTEGER NOT NULL PRIMARY KEY,
                unitId INTEGER NOT NULL,
                imageBytes BLOB NOT NULL
            )
        """.trimIndent())
    }
}
