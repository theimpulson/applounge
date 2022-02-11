package foundation.e.apps.updates

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aurora.gplayapi.data.models.AuthData
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.manager.database.fusedDownload.FusedDownload
import foundation.e.apps.manager.fused.FusedManagerRepository
import foundation.e.apps.updates.manager.UpdatesManagerRepository
import foundation.e.apps.utils.enums.Status
import foundation.e.apps.utils.enums.Type
import foundation.e.apps.utils.modules.DataStoreModule
import kotlinx.coroutines.flow.collect
import java.io.ByteArrayOutputStream
import java.net.URL

@HiltWorker
class UpdatesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val updatesManagerRepository: UpdatesManagerRepository,
    private val fusedAPIRepository: FusedAPIRepository,
    private val fusedManagerRepository: FusedManagerRepository,
    private val dataStoreModule: DataStoreModule,
    private val gson: Gson,
) : CoroutineWorker(context, params) {
    val TAG = UpdatesWorker::class.simpleName

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork: triggered")
        val authDataJson = dataStoreModule.getAuthDataSync()
        Log.d(TAG, "doWork: authdata: $authDataJson")
        val authData = gson.fromJson(authDataJson, AuthData::class.java)
        val appsNeededToUpdate = updatesManagerRepository.getUpdates(authData)
        Log.d(TAG, "doWork: update needs: ${appsNeededToUpdate.size}")

        appsNeededToUpdate.forEach { fusedApp ->
            Log.d(TAG, "doWork: triggering update for: ${fusedApp.name}")
            val downloadList = getAppDownloadLink(fusedApp, authData).toMutableList()
            val iconBase64 = fusedApp.getIconImageToBase64()

            val fusedDownload = FusedDownload(
                fusedApp._id,
                fusedApp.origin,
                fusedApp.status,
                fusedApp.name,
                fusedApp.package_name,
                downloadList,
                mutableMapOf(),
                fusedApp.status,
                fusedApp.type,
                iconBase64
            )

            fusedManagerRepository.addDownload(fusedDownload)
            Log.d(TAG, "doWork: triggering update for: ${fusedApp.name} added download in db")
            Log.d(TAG, "doWork: triggering update for: ${fusedApp.name} downloading...")
            fusedManagerRepository.downloadApp(fusedDownload)
            Log.d(TAG, "doWork: triggering update for: ${fusedApp.name} downloaded...")
        }

        fusedManagerRepository.getDownloadListFlow().collect {
            Log.d(TAG, "doWork: updated downloadlist ${it.size}")
            it.forEach { fusedDownload ->
                Log.d(TAG, "doWork: updated downloadlistitem ${fusedDownload.name}")
                if (fusedDownload.type == Type.NATIVE && fusedDownload.status == Status.INSTALLING && fusedDownload.downloadIdMap.all { it.value }) {
                    Log.d(TAG, "doWork: triggering update for: ${fusedDownload.name} installing...")
                    fusedManagerRepository.installApp(fusedDownload)
                    Log.d(TAG, "doWork: triggering update for: ${fusedDownload.name} installed")
                }
            }
        }
        return Result.success()
    }

    private suspend fun getAppDownloadLink(app: FusedApp, authData: AuthData): List<String> {
        val downloadList = mutableListOf<String>()
        if (app.type == Type.PWA) {
            downloadList.add(app.url)
        } else {
            downloadList.addAll(
                fusedAPIRepository.getDownloadLink(
                    app._id,
                    app.package_name,
                    app.latest_version_code,
                    app.offer_type,
                    authData,
                    app.origin
                )
            )
        }
        return downloadList
    }
}

fun FusedApp.getIconImageToBase64(): String {
    val stream = URL(icon_image_path).openStream()
    val bitmap = BitmapFactory.decodeStream(stream)
    val byteArrayOS = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOS)
    return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT)
}
