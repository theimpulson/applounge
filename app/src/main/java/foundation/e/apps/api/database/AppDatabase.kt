package foundation.e.apps.api.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import foundation.e.apps.api.exodus.Tracker
import foundation.e.apps.api.exodus.TrackerDao
import foundation.e.apps.api.fdroid.FdroidDao
import foundation.e.apps.api.fdroid.models.FdroidEntity

@Database(
    entities = [Tracker::class, FdroidEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackerDao(): TrackerDao
    abstract fun fdroidDao(): FdroidDao

    companion object {
        private lateinit var INSTANCE: AppDatabase

        fun getInstance(context: Context): AppDatabase {
            if (!Companion::INSTANCE.isInitialized) {
                synchronized(AppDatabase::class) {
                    INSTANCE =
                        Room.databaseBuilder(context, AppDatabase::class.java, "App_Lounge")
                            .fallbackToDestructiveMigration()
                            .build()
                }
            }
            return INSTANCE
        }
    }
}
