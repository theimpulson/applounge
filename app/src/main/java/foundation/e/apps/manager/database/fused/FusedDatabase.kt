package foundation.e.apps.manager.database.fused

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FusedDownload::class], version = 1, exportSchema = false)
abstract class FusedDatabase : RoomDatabase() {

    abstract fun fusedDao(): FusedDAO
}
