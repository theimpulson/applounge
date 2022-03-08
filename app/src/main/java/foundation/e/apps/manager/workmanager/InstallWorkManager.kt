package foundation.e.apps.manager.workmanager

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import foundation.e.apps.manager.database.fusedDownload.FusedDownload

object InstallWorkManager {
    private const val INSTALL_WORK_NAME = "APP_LOUNGE_INSTALL_APP"
    fun enqueueWork(context: Context, fusedDownload: FusedDownload) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            INSTALL_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            OneTimeWorkRequest.Builder(InstallAppWorker::class.java).setInputData(
                Data.Builder()
                    .putString(InstallAppWorker.INPUT_DATA_FUSED_DOWNLOAD, fusedDownload.id)
                    .build()
            ).build()
        )
    }
}
