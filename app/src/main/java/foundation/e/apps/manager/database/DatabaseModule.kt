package foundation.e.apps.manager.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import foundation.e.apps.manager.database.fused.FusedDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "fused_database"

    @Singleton
    @Provides
    fun provideDatabaseInstance(@ApplicationContext context: Context): FusedDatabase {
        return Room.databaseBuilder(
            context,
            FusedDatabase::class.java,
            DATABASE_NAME
        ).build()
    }
}
