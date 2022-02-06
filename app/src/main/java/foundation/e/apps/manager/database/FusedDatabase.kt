package foundation.e.apps.manager.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import foundation.e.apps.manager.database.fusedDownload.FusedDownload
import foundation.e.apps.manager.database.fusedDownload.FusedDownloadDAO

@Database(entities = [FusedDownload::class], version = 1, exportSchema = false)
@TypeConverters(FusedConverter::class)
abstract class FusedDatabase : RoomDatabase() {

    abstract fun fusedDownloadDao(): FusedDownloadDAO
}
