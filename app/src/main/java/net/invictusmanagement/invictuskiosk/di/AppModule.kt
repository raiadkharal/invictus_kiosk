package net.invictusmanagement.invictuskiosk.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.invictusmanagement.invictuskiosk.BuildConfig
import net.invictusmanagement.invictuskiosk.commons.SafeApiCaller
import net.invictusmanagement.invictuskiosk.data.local.dao.CouponsDao
import net.invictusmanagement.invictuskiosk.data.local.dao.DirectoryDao
import net.invictusmanagement.invictuskiosk.data.local.dao.HomeDao
import net.invictusmanagement.invictuskiosk.data.local.dao.ResidentsDao
import net.invictusmanagement.invictuskiosk.data.local.dao.SystemLogDao
import net.invictusmanagement.invictuskiosk.data.local.dao.VacanciesDao
import net.invictusmanagement.invictuskiosk.commons.SnapshotManager
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
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import net.invictusmanagement.invictuskiosk.util.NetworkMonitor
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
        dataStoreManager: DataStoreManager
    ): RestClient {
        return RestClient(
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
    fun provideSnapshotManager(
        @ApplicationContext context: Context,
        api: ApiInterface
    ): SnapshotManager{
        return SnapshotManager(context, api)
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
    fun provideLoginRepository(api: ApiInterface, logger: GlobalLogger): LoginRepository {
        return LoginRepositoryImpl(api, logger)
    }

    @Provides
    @Singleton
    fun provideSaferApiCaller(
        networkMonitor: NetworkMonitor,
        globalLogger: GlobalLogger
    ): SafeApiCaller {
        return SafeApiCaller(networkMonitor, globalLogger)
    }


    @Provides
    @Singleton
    fun provideHomeRepository(
        api: ApiInterface,
        homeDao: HomeDao,
        logger: GlobalLogger,
        safeApiCaller: SafeApiCaller
    ): HomeRepository {
        return HomeRepositoryImpl(api, homeDao, safeApiCaller)
    }

    @Provides
    @Singleton
    fun provideServiceKeyRepository(
        api: ApiInterface,
        safeApiCaller: SafeApiCaller
    ): ServiceKeyRepository {
        return ServiceKeyRepositoryImpl(api, safeApiCaller)
    }

    @Provides
    @Singleton
    fun provideResidentsRepository(
        api: ApiInterface,
        residentsDao: ResidentsDao,
        directoryDao: DirectoryDao,
        safeApiCaller: SafeApiCaller
    ): ResidentsRepository {
        return ResidentsRepositoryImpl(api, residentsDao, directoryDao, safeApiCaller)
    }

    @Provides
    @Singleton
    fun provideDirectoryRepository(
        api: ApiInterface,
        dao: DirectoryDao,
        safeApiCaller: SafeApiCaller
    ): DirectoryRepository {
        return DirectoryRepositoryImpl(api, dao, safeApiCaller)
    }

    @Provides
    @Singleton
    fun provideCouponsRepository(
        api: ApiInterface,
        dao: CouponsDao,
        safeApiCaller: SafeApiCaller
    ): CouponsRepository {
        return CouponsRepositoryImpl(api, dao, safeApiCaller)
    }

    @Provides
    @Singleton
    fun provideVacancyRepository(
        @ApplicationContext context: Context,
        api: ApiInterface,
        dao: VacanciesDao,
        logger: GlobalLogger,
        safeApiCaller: SafeApiCaller
    ): VacancyRepository {
        return VacancyRepositoryImpl(context, api, dao, logger, safeApiCaller)
    }

    @Provides
    @Singleton
    fun provideVideoCallRepository(
        api: ApiInterface,
        safeApiCaller: SafeApiCaller
    ): VideoCallRepository {
        return VideoCallRepositoryImpl(api, safeApiCaller)
    }

    @Provides
    @Singleton
    fun provideVoiceMailRepository(
        api: ApiInterface,
        safeApiCaller: SafeApiCaller
    ): VoicemailRepository {
        return VoicemailRepositoryImpl(api, safeApiCaller)
    }

    @Provides
    @Singleton
    fun provideUnitMapRepository(
        @ApplicationContext context: Context,
        api: ApiInterface,
        safeApiCaller: SafeApiCaller,
        vacanciesDao: VacanciesDao
    ): UnitMapRepository {
        return UnitMapRepositoryImpl(context, api, vacanciesDao, safeApiCaller)
    }

    @Provides
    @Singleton
    fun provideScreenSaverRepository(): ScreenSaverRepository {
        return ScreenSaverRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideLogRepository(
        mobileApi: MobileApiInterface,
        api: ApiInterface,
        systemLogDao: SystemLogDao
    ): LogRepository {
        return LogRepositoryImpl(mobileApi, api, systemLogDao)
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
    fun provideRelayManager(
        @ApplicationContext context: Context,
        globalLogger: GlobalLogger
    ): RelayManager {
        return RelayManager(context, globalLogger)
    }

    @Provides
    @Singleton
    fun provideRelayRepository(
        relayManager: RelayManager,
        globalLogger: GlobalLogger
    ): RelayManagerRepository {
        return RelayManagerRepositoryImpl(relayManager, globalLogger)
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context,
        globalLogger: GlobalLogger
    ): NetworkMonitor {
        return NetworkMonitor(context, globalLogger)
    }

}