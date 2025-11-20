package net.invictusmanagement.invictuskiosk.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.invictusmanagement.invictuskiosk.BuildConfig
import net.invictusmanagement.invictuskiosk.data.local.dao.CouponsDao
import net.invictusmanagement.invictuskiosk.data.local.dao.DirectoryDao
import net.invictusmanagement.invictuskiosk.data.local.dao.HomeDao
import net.invictusmanagement.invictuskiosk.data.local.dao.VacanciesDao
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.MobileApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.MobileRestClient
import net.invictusmanagement.invictuskiosk.data.remote.RestClient
import net.invictusmanagement.invictuskiosk.data.repository.CouponsRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.DirectoryRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.HomeRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.LogRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.LoginRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.RelayManagerRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.ResidentsRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.ScreenSaverRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.ServiceKeyRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.UnitMapRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.VacancyRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.VideoCallRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.VoicemailRepositoryImpl
import net.invictusmanagement.invictuskiosk.domain.repository.CouponsRepository
import net.invictusmanagement.invictuskiosk.domain.repository.DirectoryRepository
import net.invictusmanagement.invictuskiosk.domain.repository.HomeRepository
import net.invictusmanagement.invictuskiosk.domain.repository.LogRepository
import net.invictusmanagement.invictuskiosk.domain.repository.LoginRepository
import net.invictusmanagement.invictuskiosk.domain.repository.RelayManagerRepository
import net.invictusmanagement.invictuskiosk.domain.repository.ResidentsRepository
import net.invictusmanagement.invictuskiosk.domain.repository.ScreenSaverRepository
import net.invictusmanagement.invictuskiosk.domain.repository.ServiceKeyRepository
import net.invictusmanagement.invictuskiosk.domain.repository.UnitMapRepository
import net.invictusmanagement.invictuskiosk.domain.repository.VacancyRepository
import net.invictusmanagement.invictuskiosk.domain.repository.VideoCallRepository
import net.invictusmanagement.invictuskiosk.domain.repository.VoicemailRepository
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import net.invictusmanagement.invictuskiosk.util.NetworkMonitor
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import net.invictusmanagement.relaymanager.RelayManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }

    @Provides
    @Singleton
    fun provideRestClient(
        @ApplicationContext context: Context,
        dataStoreManager: DataStoreManager
    ): RestClient {
        return RestClient(
            context,
            baseUrl = BuildConfig._baseUrl,
            dataStoreManager = dataStoreManager
        )
    }

    @Provides
    @Singleton
    fun provideApiInterface(
        restClient: RestClient
    ): ApiInterface {
        return restClient.createApi()
    }

    @Provides
    @Singleton
    fun provideMobileRestClient(): MobileRestClient {
        return MobileRestClient(
            baseUrl = BuildConfig._mobileBaseUrl,
        )
    }

    @Provides
    @Singleton
    fun provideMobileApiInterface(
        mobileRestClient: MobileRestClient
    ): MobileApiInterface {
        return mobileRestClient.createApi()
    }

    @Provides
    @Singleton
    fun provideLoginRepository(api: ApiInterface): LoginRepository {
        return LoginRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideHomeRepository(api: ApiInterface,homeDao: HomeDao): HomeRepository {
        return HomeRepositoryImpl(api,homeDao)
    }

    @Provides
    @Singleton
    fun provideServiceKeyRepository(api: ApiInterface): ServiceKeyRepository {
        return ServiceKeyRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideResidentsRepository(api: ApiInterface): ResidentsRepository {
        return ResidentsRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideDirectoryRepository(api: ApiInterface,directoryDao: DirectoryDao): DirectoryRepository {
        return DirectoryRepositoryImpl(api,directoryDao)
    }

    @Provides
    @Singleton
    fun provideCouponsRepository(api: ApiInterface,couponsDao: CouponsDao): CouponsRepository {
        return CouponsRepositoryImpl(api,couponsDao)
    }

    @Provides
    @Singleton
    fun provideVacancyRepository(api: ApiInterface,vacanciesDao: VacanciesDao): VacancyRepository {
        return VacancyRepositoryImpl(api,vacanciesDao)
    }

    @Provides
    @Singleton
    fun provideVideoCallRepository(api: ApiInterface): VideoCallRepository {
        return VideoCallRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideVoiceMailRepository(api: ApiInterface): VoicemailRepository {
        return VoicemailRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideUnitMapRepository(api: ApiInterface): UnitMapRepository {
        return UnitMapRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideScreenSaverRepository(): ScreenSaverRepository {
        return ScreenSaverRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideLogRepository(
        api: MobileApiInterface,
    ): LogRepository {
        return LogRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideGlobalLogger(
        logRepository: LogRepository,
        dataStoreManager: DataStoreManager
    ): GlobalLogger {
        return GlobalLogger(logRepository, dataStoreManager)
    }

    @Provides
    @Singleton
    fun provideRelayManager(@ApplicationContext context: Context,globalLogger: GlobalLogger): RelayManager {
        return RelayManager(context,globalLogger)
    }

    @Provides
    @Singleton
    fun provideRelayRepository(relayManager: RelayManager,globalLogger: GlobalLogger): RelayManagerRepository {
        return RelayManagerRepositoryImpl(relayManager,globalLogger)
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitor(context)
    }

}