package io.eelo.appinstaller.application.model

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import io.eelo.appinstaller.api.AppDetailRequest
import io.eelo.appinstaller.api.PackageNameSearchRequest
import io.eelo.appinstaller.application.model.State.*
import io.eelo.appinstaller.application.model.data.BasicData
import io.eelo.appinstaller.application.model.data.FullData
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Error
import java.util.concurrent.atomic.AtomicInteger

class Application(val packageName: String, private val applicationManager: ApplicationManager) {

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
    fun buttonClicked(activity: Activity) {
        when (stateManager.state) {
            INSTALLED -> info.launch(activity)
            DOWNLOADED -> applicationManager.install(this)
            NOT_UPDATED, NOT_DOWNLOADED -> applicationManager.download(this)
            INSTALLING -> {
                applicationManager.stopInstalling(this)
                return
            }
            DOWNLOADING -> {
                applicationManager.stopDownloading(this)
                downloader?.cancel()
                downloader = null
                return
            }
        }
        stateManager.find(activity, basicData!!)
    }

    fun download(context: Context) {
        assertFullData(context)
        downloader = Downloader()
        stateManager.notifyDownloading(downloader!!)
        try {
            val canceled = downloader!!.download(context, fullData!!, info.getApkFile(context, basicData!!))
            downloader = null
            if (!canceled) {
                applicationManager.install(this)
            }
            else {
                info.getApkFile(context, basicData!!).delete()
            }
        } catch (e: Exception) {
            stateManager.notifyError()
        }
        stateManager.find(context, basicData!!)
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
        stateManager.find(context, basicData)
    }

    fun update(fullData: FullData, context: Context) {
        this.fullData = fullData
        update(fullData.basicData, context)
        fullData.basicData = basicData!!
    }
}
