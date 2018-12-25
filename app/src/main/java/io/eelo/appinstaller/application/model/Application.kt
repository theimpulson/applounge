package io.eelo.appinstaller.application.model

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.ImageView
import io.eelo.appinstaller.api.AppDetailRequest
import io.eelo.appinstaller.api.PackageNameSearchRequest
import io.eelo.appinstaller.application.model.State.*
import io.eelo.appinstaller.application.model.data.BasicData
import io.eelo.appinstaller.application.model.data.FullData
import io.eelo.appinstaller.utils.Error
import io.eelo.appinstaller.utils.Constants
import java.util.concurrent.atomic.AtomicInteger

class Application(val packageName: String, private val installManager: InstallManager) {

    private val uses = AtomicInteger(0)
    private val info = ApplicationInfo(packageName)
    private val stateManager = StateManager(info, this)

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

    fun incrementUses() {
        uses.incrementAndGet()
    }

    fun decrementUses() {
        uses.decrementAndGet()
        installManager.tryRemove(this)
    }

    @Synchronized
    fun buttonClicked(activity: Activity) {
        when (stateManager.state) {
            INSTALLED -> info.launch(activity)
            DOWNLOADED -> {
                prepareInstall()
            }
            NOT_UPDATED, NOT_DOWNLOADED -> {
                if (canWriteStorage(activity)) {
                    stateManager.changeState(DOWNLOADING)
                    installManager.download(packageName)
                }
            }
            DOWNLOADING -> {
                stateManager.changeState(NOT_DOWNLOADED)
                installManager.removeDownload(packageName)
                cancelDownload()
            }
            INSTALLING -> {
            }
        }
    }

    private fun canWriteStorage(activity: Activity): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.STORAGE_PERMISSION_REQUEST_CODE)
                false
            } else {
                true
            }
        } else {
            true
        }
    }

    private fun cancelDownload() {
        if (downloader != null) {
            downloader!!.cancel()
        }
    }

    fun download(context: Context) {
        assertFullData(context)
        downloader = Downloader()
        stateManager.notifyDownloading(downloader!!)
        try {
            downloader!!.download(fullData!!, info.getApkFile(basicData!!))
            downloader = null
            prepareInstall()
        } catch (e: Exception) {
            e.printStackTrace()
            stateManager.find(context, basicData!!)
            stateManager.notifyError()
        }
    }

    private fun prepareInstall() {
        stateManager.changeState(INSTALLING)
        installManager.install(packageName)
    }

    fun install(context: Context) {
        info.install(context, basicData!!)
        stateManager.find(context, basicData!!)
    }

    fun isUsed(): Boolean {
        return uses.get() != 0
    }

    fun assertFullData(context: Context): Error? {
        if (fullData != null) {
            return null
        }
        return findFullData(context)
    }

    private fun findBasicData(context: Context): Error? {
        var error: Error? = null
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
        stateManager.find(context, basicData)
    }

    fun update(fullData: FullData, context: Context) {
        this.fullData = fullData
        update(fullData.basicData, context)
        fullData.basicData = basicData!!
    }
}
