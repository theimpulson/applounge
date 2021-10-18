package foundation.e.apps.api.cleanapk

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    /**
     * Provides an instance of Retrofit to work with CleanAPK API
     * @return instance of [CleanAPKInterface]
     */
    @Singleton
    @Provides
    fun provideCleanAPKInterface(okHttpClient: OkHttpClient): CleanAPKInterface {
        return Retrofit.Builder()
            .baseUrl(CleanAPKInterface.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CleanAPKInterface::class.java)
    }

    @Singleton
    @Provides
    fun provideCache(@ApplicationContext context: Context): Cache {
        val cacheSize = (10 * 1024 * 1024).toLong() // 10 MB
        return Cache(context.cacheDir, cacheSize)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(cache: Cache): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .build()
    }
}
