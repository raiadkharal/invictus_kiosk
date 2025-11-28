package net.invictusmanagement.invictuskiosk.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RestClient(
    baseUrl: String,
    dataStoreManager: DataStoreManager
) {

    // Keep token in memory (interceptor will read instantly)
    @Volatile
    private var cachedToken: String? = null

    init {
        // Sync DataStore token into memory
        CoroutineScope(Dispatchers.IO).launch {
            dataStoreManager.accessTokenFlow.collectLatest { token ->
                cachedToken = token
            }
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder().apply {
            cachedToken?.let { token ->
                addHeader("Authorization", "Bearer $token")
            }
        }.build()

        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Or NONE for release builds
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    fun createApi(): ApiInterface = retrofit.create(ApiInterface::class.java)
}
