package foundation.e.apps.manager.database.fused

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface FusedDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addDownload(fusedDownload: FusedDownload)

    @Query("SELECT * FROM fuseddownload")
    fun getDownloadLiveList(): LiveData<List<FusedDownload>>

    @Query("SELECT * FROM fuseddownload")
    suspend fun getDownloadList(): List<FusedDownload>

    @Update
    suspend fun updateDownload(fusedDownload: FusedDownload)

    @Delete
    suspend fun deleteDownload(fusedDownload: FusedDownload)
}
