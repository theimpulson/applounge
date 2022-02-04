package foundation.e.apps.manager.database

import androidx.lifecycle.LiveData
import foundation.e.apps.manager.database.fused.FusedDatabase
import foundation.e.apps.manager.database.fused.FusedDownload
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
    private val fusedDatabase: FusedDatabase
) {

    suspend fun addDownload(fusedDownload: FusedDownload) {
        return fusedDatabase.fusedDao().addDownload(fusedDownload)
    }

    suspend fun getDownloadList(): List<FusedDownload> {
        return fusedDatabase.fusedDao().getDownloadList()
    }

    fun getDownloadLiveList(): LiveData<List<FusedDownload>> {
        return fusedDatabase.fusedDao().getDownloadLiveList()
    }

    suspend fun updateDownload(fusedDownload: FusedDownload) {
        return fusedDatabase.fusedDao().updateDownload(fusedDownload)
    }

    suspend fun deleteDownload(fusedDownload: FusedDownload) {
        return fusedDatabase.fusedDao().deleteDownload(fusedDownload)
    }
}
