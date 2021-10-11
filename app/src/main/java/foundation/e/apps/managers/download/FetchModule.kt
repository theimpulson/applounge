package foundation.e.apps.managers.download

import android.content.Context
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FetchModule {

    /**
     * Provides configuration to get an instance of [Fetch]
     * @return [FetchConfiguration]
     */
    @Singleton
    @Provides
    fun provideFetchConfiguration(
        @ApplicationContext
        context: Context
    ): FetchConfiguration {
        return FetchConfiguration.Builder(context)
            .setInternetAccessUrlCheck("https://connectivity.ecloud.global")
            .setDownloadConcurrentLimit(1)
            .build()
    }

    /**
     * Provides an instance of [Fetch] using required configuration
     * @return an instance of [Fetch]
     */
    @Singleton
    @Provides
    fun provideFetchInstance(fetchConfiguration: FetchConfiguration): Fetch {
        return Fetch.Impl.getInstance(fetchConfiguration)
    }
}