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
import java.io.IOException
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

    fun download(context: Context) {
        assertFullData(context)
        downloader = Downloader()
        stateManager.notifyDownloading(downloader!!)
        try {
            downloader!!.download(fullData!!, info.getApkFile(basicData!!))
            downloader = null
            prepareInstall()
        } catch (e: IOException) {
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

    fun getBasicData(context: Context): BasicData? {
        if (basicData == null) {
            val found = findFullData(context)
            if (!found) {
                return null
            }
        }
        return basicData!!
    }

    fun getFullData(context: Context): FullData? {
        if (fullData == null) {
            val found = findFullData(context)
            if (!found) {
                return null
            }
        }
        return fullData!!
    }

    private fun assertBasicData(context: Context): Boolean {
        return basicData != null || findFullData(context)
    }

    fun assertFullData(context: Context): Boolean {
        return fullData != null || findFullData(context)
    }

    private fun findFullData(context: Context): Boolean {
        if (basicData == null) {
            var fullData: FullData? = null
            PackageNameSearchRequest(packageName).request { applicationError, searchResult ->
                when (applicationError) {
                    null -> {
                        fullData = searchResult!!.findOneAppData(packageName)
                    }
                    Error.SERVER_UNAVAILABLE -> {
                        // TODO Handle error
                    }
                    Error.REQUEST_TIMEOUT -> {
                        // TODO Handle error
                    }
                    Error.UNKNOWN -> {
                        // TODO Handle error
                    }
                    else -> {
                        // TODO Handle error
                    }
                }
            }
            fullData?.let {
                update(it, context)
                return true
            }
            return false
        } else {
            AppDetailRequest(basicData!!.id).request { applicationError, fullData ->
                when (applicationError) {
                    null -> {
                        update(fullData!!, context)
                    }
                    Error.SERVER_UNAVAILABLE -> {
                        // TODO Handle error
                    }
                    Error.REQUEST_TIMEOUT -> {
                        // TODO Handle error
                    }
                    Error.UNKNOWN -> {
                        // TODO Handle error
                    }
                    else -> {
                        // TODO Handle error
                    }
                }
            }
            return true
        }
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
