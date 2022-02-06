package foundation.e.apps.manager.database

import androidx.lifecycle.LiveData
import foundation.e.apps.manager.database.fusedDownload.FusedDownload
import foundation.e.apps.manager.database.fusedDownload.FusedDownloadDAO
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
    private val fusedDownloadDAO: FusedDownloadDAO
) {

    suspend fun addDownload(fusedDownload: FusedDownload) {
        return fusedDownloadDAO.addDownload(fusedDownload)
    }

    suspend fun getDownloadList(): List<FusedDownload> {
        return fusedDownloadDAO.getDownloadList()
    }

    fun getDownloadLiveList(): LiveData<List<FusedDownload>> {
        return fusedDownloadDAO.getDownloadLiveList()
    }

    suspend fun updateDownload(fusedDownload: FusedDownload) {
        return fusedDownloadDAO.updateDownload(fusedDownload)
    }

    suspend fun deleteDownload(fusedDownload: FusedDownload) {
        return fusedDownloadDAO.deleteDownload(fusedDownload)
    }
}
