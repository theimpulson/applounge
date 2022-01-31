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
    fun getDownloadList(): LiveData<List<FusedDownload>>

    @Query("SELECT * FROM fuseddownload WHERE downloadId=:downloadId")
    suspend fun getDownloadByID(downloadId: Long): List<FusedDownload>

    @Query("SELECT * FROM fuseddownload WHERE package_name=:packageName")
    suspend fun getDownloadByPkg(packageName: String): List<FusedDownload>

    @Update
    suspend fun updateDownload(fusedDownload: FusedDownload)

    @Delete
    suspend fun deleteDownload(fusedDownload: FusedDownload)
}
