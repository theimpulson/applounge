package foundation.e.apps.utils.download

import android.app.DownloadManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DownloadManagerModule {

    @Provides
    @Singleton
    fun provideDownloadManagerInstance(@ApplicationContext context: Context): DownloadManager {
        return context.getSystemService(DownloadManager::class.java)
    }

    @Provides
    @Singleton
    fun provideDownloadManagerQueryInstance(): DownloadManager.Query {
        return DownloadManager.Query()
    }
}