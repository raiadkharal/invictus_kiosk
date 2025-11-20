package net.invictusmanagement.invictuskiosk.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.invictusmanagement.invictuskiosk.data.local.AppDatabase
import net.invictusmanagement.invictuskiosk.data.local.dao.CouponsDao
import net.invictusmanagement.invictuskiosk.data.local.dao.DirectoryDao
import net.invictusmanagement.invictuskiosk.data.local.dao.HomeDao
import net.invictusmanagement.invictuskiosk.data.local.dao.VacanciesDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "invictus_kiosk.db"
        ).build()

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
}
