package net.invictusmanagement.invictuskiosk.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.invictusmanagement.invictuskiosk.BuildConfig
import net.invictusmanagement.invictuskiosk.data.local.AppDatabase
import net.invictusmanagement.invictuskiosk.data.local.dao.CouponsDao
import net.invictusmanagement.invictuskiosk.data.local.dao.DirectoryDao
import net.invictusmanagement.invictuskiosk.data.local.dao.HomeDao
import net.invictusmanagement.invictuskiosk.data.local.dao.ResidentsDao
import net.invictusmanagement.invictuskiosk.data.local.dao.VacanciesDao
import net.invictusmanagement.invictuskiosk.data.local.migration3to4
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {

        val builder = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "invictus_kiosk.db"
        )
            .addMigrations(migration3to4)

        // Enable destructive migration ONLY in debug
        if (BuildConfig.DEBUG) {
            builder.fallbackToDestructiveMigration(true)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideHomeDao(db: AppDatabase): HomeDao = db.homeDao()

    @Provides
    @Singleton
    fun provideDirectoryDao(db: AppDatabase): DirectoryDao = db.directoryDao()

    @Provides
    @Singleton
    fun provideCouponsDao(db: AppDatabase): CouponsDao = db.couponDao()

    @Provides
    @Singleton
    fun provideVacanciesDao(db: AppDatabase): VacanciesDao = db.vacanciesDao()

    @Provides
    @Singleton
    fun provideResidentsDao(db: AppDatabase): ResidentsDao = db.residentsDao()

    @Provides
    @Singleton
    fun provideSystemLogDao(db: AppDatabase) = db.systemLogDao()
}
