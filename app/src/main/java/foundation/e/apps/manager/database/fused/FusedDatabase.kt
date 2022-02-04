package foundation.e.apps.manager.database.fused

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [FusedDownload::class], version = 1, exportSchema = false)
@TypeConverters(FusedConverter::class)
abstract class FusedDatabase : RoomDatabase() {

    abstract fun fusedDao(): FusedDAO
}
