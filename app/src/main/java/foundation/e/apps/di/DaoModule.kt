package foundation.e.apps.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import foundation.e.apps.api.database.AppDatabase
import foundation.e.apps.api.exodus.TrackerDao

@InstallIn(SingletonComponent::class)
@Module
object DaoModule {
    @Provides
    fun getTrackerDao(@ApplicationContext context: Context): TrackerDao {
        return AppDatabase.getInstance(context).trackerDao()
    }
}
