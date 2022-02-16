package foundation.e.apps.updates.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aurora.gplayapi.data.models.AuthData
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import foundation.e.apps.R
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.manager.database.fusedDownload.FusedDownload
import foundation.e.apps.manager.fused.FusedManagerRepository
import foundation.e.apps.updates.UpdatesNotifier
import foundation.e.apps.utils.enums.Origin
import foundation.e.apps.utils.enums.Status
import foundation.e.apps.utils.enums.Type
import foundation.e.apps.utils.modules.DataStoreModule
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
    private var shouldShowNotification = true
    private var automaticInstallEnabled = true
    private var onlyOnUnmeteredNetwork = false

    override suspend fun doWork(): Result {
        return try {
            checkForUpdates()
            Result.success()
        } catch (e: Throwable) {
            Result.failure()
        }
    }

    private suspend fun checkForUpdates() {
        loadSettings()
        val authData = getAuthData()
        val appsNeededToUpdate = updatesManagerRepository.getUpdates(authData)
        val isConnectedToUnmeteredNetwork = isConnectedToUnmeteredNetwork(applicationContext)
        handleNotification(appsNeededToUpdate, isConnectedToUnmeteredNetwork)
        triggerUpdateProcessOnSettings(
            isConnectedToUnmeteredNetwork,
            appsNeededToUpdate,
            authData
        )
    }

    private suspend fun triggerUpdateProcessOnSettings(
        isConnectedToUnmeteredNetwork: Boolean,
        appsNeededToUpdate: List<FusedApp>,
        authData: AuthData
    ) {
        if (automaticInstallEnabled &&
            applicationContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        ) {
            if (onlyOnUnmeteredNetwork && isConnectedToUnmeteredNetwork) {
                startUpdateProcess(appsNeededToUpdate, authData)
            } else if (!onlyOnUnmeteredNetwork) {
                startUpdateProcess(appsNeededToUpdate, authData)
            }
        }
    }

    private fun handleNotification(
        appsNeededToUpdate: List<FusedApp>,
        isConnectedToUnmeteredNetwork: Boolean
    ) {
        if (appsNeededToUpdate.isNotEmpty()) {
            UpdatesNotifier().showNotification(
                applicationContext,
                appsNeededToUpdate.size,
                automaticInstallEnabled,
                onlyOnUnmeteredNetwork,
                isConnectedToUnmeteredNetwork
            )
        }
    }

    private fun getAuthData(): AuthData {
        val authDataJson = dataStoreModule.getAuthDataSync()
        return gson.fromJson(authDataJson, AuthData::class.java)
    }

    private suspend fun startUpdateProcess(
        appsNeededToUpdate: List<FusedApp>,
        authData: AuthData
    ) {
        appsNeededToUpdate.forEach { fusedApp ->
            val downloadList = getAppDownloadLink(fusedApp, authData).toMutableList()
            val iconBase64 = getIconImageToBase64(fusedApp)

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
            Log.d(TAG, "doWork: triggering update for: ${fusedApp.name} downloading...")
            fusedManagerRepository.downloadApp(fusedDownload)
        }
        observeFusedDownload()
    }

    private suspend fun observeFusedDownload() {
        fusedManagerRepository.getDownloadListFlow().collect {
            it.forEach { fusedDownload ->
                checkForInstall(fusedDownload)
            }
        }
    }

    private fun checkForInstall(fusedDownload: FusedDownload) {
        if (fusedDownload.type == Type.NATIVE && fusedDownload.status == Status.INSTALLING && fusedDownload.downloadIdMap.all { it.value }) {
            Log.d(TAG, "doWork: triggering update for: ${fusedDownload.name} installing...")
            fusedManagerRepository.installApp(fusedDownload)
            Log.d(TAG, "doWork: triggering update for: ${fusedDownload.name} installed")
        }
    }

    private fun loadSettings() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        shouldShowNotification =
            preferences.getBoolean(
                applicationContext.getString(
                    R.string.updateNotify
                ),
                true
            )
        automaticInstallEnabled = preferences.getBoolean(
            applicationContext.getString(
                R.string.auto_install_enabled
            ),
            true
        )

        onlyOnUnmeteredNetwork = preferences.getBoolean(
            applicationContext.getString(
                R.string.only_unmetered_network
            ),
            false
        )
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

    private fun getIconImageToBase64(fusedApp: FusedApp): String {
        val url =
            if (fusedApp.origin == Origin.CLEANAPK) "${CleanAPKInterface.ASSET_URL}${fusedApp.icon_image_path}" else fusedApp.icon_image_path
        val stream = URL(url).openStream()
        val bitmap = BitmapFactory.decodeStream(stream)
        val byteArrayOS = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOS)
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT)
    }

    /*
     * Checks if the device is connected to a metered connection or not
     * @param context current Context
     * @return returns true if the connections is not metered, false otherwise
     */
    private fun isConnectedToUnmeteredNetwork(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) {
                return true
            }
        }
        return false
    }
}
