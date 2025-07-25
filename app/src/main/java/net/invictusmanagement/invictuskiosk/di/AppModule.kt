package net.invictusmanagement.invictuskiosk.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.invictusmanagement.invictuskiosk.commons.Constants
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.RestClient
import net.invictusmanagement.invictuskiosk.data.repository.CouponsRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.DirectoryRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.HomeRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.LoginRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.ResidentsRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.ServiceKeyRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.UnitMapRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.VacancyRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.VideoCallRepositoryImpl
import net.invictusmanagement.invictuskiosk.data.repository.VoicemailRepositoryImpl
import net.invictusmanagement.invictuskiosk.domain.repository.CouponsRepository
import net.invictusmanagement.invictuskiosk.domain.repository.DirectoryRepository
import net.invictusmanagement.invictuskiosk.domain.repository.HomeRepository
import net.invictusmanagement.invictuskiosk.domain.repository.LoginRepository
import net.invictusmanagement.invictuskiosk.domain.repository.ResidentsRepository
import net.invictusmanagement.invictuskiosk.domain.repository.ServiceKeyRepository
import net.invictusmanagement.invictuskiosk.domain.repository.UnitMapRepository
import net.invictusmanagement.invictuskiosk.domain.repository.VacancyRepository
import net.invictusmanagement.invictuskiosk.domain.repository.VideoCallRepository
import net.invictusmanagement.invictuskiosk.domain.repository.VoicemailRepository
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
            baseUrl = Constants.BASE_URL,
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
    fun provideLoginRepository(api: ApiInterface): LoginRepository {
        return LoginRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideHomeRepository(api: ApiInterface): HomeRepository {
        return HomeRepositoryImpl(api)
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
    fun provideDirectoryRepository(api: ApiInterface): DirectoryRepository {
        return DirectoryRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideCouponsRepository(api: ApiInterface): CouponsRepository {
        return CouponsRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideVacancyRepository(api: ApiInterface): VacancyRepository {
        return VacancyRepositoryImpl(api)
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

}