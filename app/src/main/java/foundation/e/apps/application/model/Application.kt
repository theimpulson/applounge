package foundation.e.apps.application.model

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.ImageView
import foundation.e.apps.api.AppDetailRequest
import foundation.e.apps.api.PackageNameSearchRequest
import foundation.e.apps.application.model.State.*
import foundation.e.apps.application.model.data.BasicData
import foundation.e.apps.application.model.data.FullData
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error
import java.util.concurrent.atomic.AtomicInteger

class Application(val packageName: String, private val applicationManager: ApplicationManager) :
        DownloaderInterface, InstallerInterface {

    private val uses = AtomicInteger(0)
    private val info = ApplicationInfo(packageName)
    private val stateManager = StateManager(info, this, applicationManager)

    var basicData: BasicData? = null
    var fullData: FullData? = null

    fun addListener(listener: ApplicationStateListener) {
        stateManager.addListener(listener)
    }

    fun removeListener(listener: ApplicationStateListener) {
        stateManager.removeListener(listener)
    }

    val state: State
        get() = stateManager.state
    var downloader: Downloader? = null
    private val blocker = Object()

    fun incrementUses() {
        uses.incrementAndGet()
    }

    fun decrementUses() {
        uses.decrementAndGet()
        applicationManager.tryRemove(this)
    }

    fun checkForStateUpdate(context: Context) {
        if (basicData != null) {
            stateManager.find(context, basicData!!)
        }
    }

    @Synchronized
    fun buttonClicked(context: Context, activity: Activity?) {
        when (stateManager.state) {
            INSTALLED -> info.launch(context)
            NOT_UPDATED, NOT_DOWNLOADED -> {
                if (activity != null) {
                    if (canWriteStorage(activity)) {
                        applicationManager.install(context, this)
                    }
                } else {
                    applicationManager.install(context, this)
                }
            }
            INSTALLING -> {
                if (downloader != null) {
                    downloader?.cancelDownload()
                } else {
                    onDownloadComplete(context, DownloadManager.STATUS_FAILED)
                }
                return
            }
        }
        checkForStateUpdate(context)
    }

    private fun canWriteStorage(activity: Activity): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        Constants.STORAGE_PERMISSION_REQUEST_CODE)
                false
            } else {
                true
            }
        } else {
            true
        }
    }

    fun download(context: Context) {
        val error = assertFullData(context)
        if (error == null) {
            downloader = Downloader(info, fullData!!, this)
            stateManager.notifyDownloading(downloader!!)
            downloader!!.download(context)
            synchronized(blocker) {
                blocker.wait()
            }
        } else {
            stateManager.notifyError(error)
            onDownloadComplete(context, DownloadManager.STATUS_FAILED)
        }
    }

    override fun onDownloadComplete(context: Context, status: Int) {
        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            install(context)
        } else {
            synchronized(blocker) {
                blocker.notify()
            }
            info.getApkFile(context, basicData!!).delete()
            applicationManager.stopInstalling(context, this)
        }
        downloader = null
    }

    private fun install(context: Context) {
        info.install(context, basicData!!, this)
    }

    override fun onInstallationComplete(context: Context) {
        synchronized(blocker) {
            blocker.notify()
        }
        info.getApkFile(context, basicData!!).delete()
    }

    fun isUsed(): Boolean {
        return uses.get() != 0
    }

    fun assertBasicData(context: Context): Error? {
        if (basicData != null) {
            return null
        }
        return findBasicData(context)
    }

    fun assertFullData(context: Context): Error? {
        if (fullData != null) {
            return null
        }
        return findFullData(context)
    }

    private fun findBasicData(context: Context): Error? {
        var error: Error? = null
        if (Common.isNetworkAvailable(context)) {
            PackageNameSearchRequest(packageName).request { applicationError, searchResult ->
                when (applicationError) {
                    null -> {
                        error = Error.NO_RESULTS
                        searchResult!!.findOneAppData(packageName)?.let {
                            update(it, context)
                            error = null
                        }
                    }
                    else -> {
                        error = applicationError
                    }
                }
            }
        } else {
            error = Error.NO_INTERNET
        }
        return error
    }

    private fun findFullData(context: Context): Error? {
        if (basicData == null) {
            val error = findBasicData(context)
            if (error != null) {
                return error
            }
        }
        var error: Error? = null
        if (Common.isNetworkAvailable(context)) {
            AppDetailRequest(basicData!!.id).request { applicationError, fullData ->
                when (applicationError) {
                    null -> {
                        error = Error.NO_RESULTS
                        fullData!!.let {
                            update(fullData, context)
                            error = null
                        }
                    }
                    else -> {
                        error = applicationError
                    }
                }
            }
        } else {
            error = Error.NO_INTERNET
        }
        return error
    }

    fun loadIcon(view: ImageView) {
        basicData?.loadIconAsync {
            view.setImageBitmap(it)
        }
    }

    fun update(basicData: BasicData, context: Context) {
        this.basicData?.let { basicData.updateLoadedImages(it) }
        this.basicData = basicData
        checkForStateUpdate(context)
    }

    fun update(fullData: FullData, context: Context) {
        this.fullData = fullData
        update(fullData.basicData, context)
        fullData.basicData = basicData!!
    }
}
