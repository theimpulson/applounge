package foundation.e.apps.manager.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import foundation.e.apps.manager.database.DatabaseRepository
import foundation.e.apps.manager.fused.FusedManagerRepository
import foundation.e.apps.manager.pkg.PkgManagerModule
import foundation.e.apps.utils.enums.Status
import foundation.e.apps.utils.modules.PWAManagerModule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import javax.inject.Named

class InstallAppWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val packagemanagerModule: PkgManagerModule,
    private val pwaManagerModule: PWAManagerModule,
    private val databaseRepository: DatabaseRepository,
    private val fusedManagerRepository: FusedManagerRepository
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "InstallWorker"
        const val INPUT_DATA_FUSED_DOWNLOAD = "input_data_fused_download"
    }

    override suspend fun doWork(): Result {
        val fusedDownloadString = params.inputData.getString(INPUT_DATA_FUSED_DOWNLOAD) ?: ""
        Log.d(TAG, "Fused download name $fusedDownloadString")

        val fusedDownload = databaseRepository.getDownloadById(fusedDownloadString)
        fusedDownload?.let {
            fusedManagerRepository.downloadApp(it)
            var isDownloading = true
            databaseRepository.getDownloadFlowById(it.id).collect { fusedDownload ->
                when(it.status) {
                    Status.INSTALLING -> {
                        fusedManagerRepository.installApp(fusedDownload)
                        isDownloading = false
                    }
                    Status.INSTALLED,Status.INSTALLATION_ISSUE -> {
                        isDownloading = false
                    }
                    else -> {
                        Log.wtf(TAG, "${fusedDownload.name} is in wrong state")
                    }
                }
            }

            while (isDownloading) {
                delay(2000)
            }
        }

        return Result.success()
    }

}