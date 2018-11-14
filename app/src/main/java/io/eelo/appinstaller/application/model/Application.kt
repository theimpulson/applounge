package io.eelo.appinstaller.application.model

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.ImageView
import io.eelo.appinstaller.application.model.State.*
import io.eelo.appinstaller.utils.Constants
import io.eelo.appinstaller.utils.Execute
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class Application(val data: ApplicationData, context: Context, private val installManager: InstallManager) {

    private val uses = AtomicInteger(0)
    private val info = ApplicationInfo(data)
    private val stateManager = StateManager(info, this)

    init {
        // TODO must change these lines, when API give the application's version
//        if (data.assertFullData()) {
//            stateManager.find(context)
//        }
    }

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
    fun buttonClicked(context: Context, activity: Activity?) {
        when (stateManager.state) {
            INSTALLED -> info.launch(context)
            DOWNLOADED -> {
                stateManager.changeState(INSTALLING)
                installManager.install(data.packageName)
            }
            NOT_UPDATED, NOT_DOWNLOADED -> {
                if (canWriteStorage(activity)) {
                    stateManager.changeState(DOWNLOADING)
                    installManager.download(data.packageName)
                }
            }
            DOWNLOADING -> {

            }
            INSTALLING -> {
            }
        }
    }

    private fun canWriteStorage(activity: Activity?): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (activity!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
        searchFullData(context)
        downloader = info.createDownloader()
        stateManager.notifyDownloading(downloader!!)
        try {
            downloader!!.download()
            stateManager.changeState(INSTALLING)
            installManager.install(data.packageName)
            stateManager.find(context)
        } catch (e: IOException) {
            e.printStackTrace()
            stateManager.find(context)
            stateManager.notifyError()
        }
    }

    fun install(context: Context) {
        info.install(context)
        stateManager.find(context)
    }

    fun isUsed(): Boolean {
        return uses.get() != 0
    }

    fun searchFullData(context: Context): Boolean {
        val assertFullData = data.assertFullData()
        if (assertFullData && state == INSTALLED) {
            stateManager.find(context)
        }
        return assertFullData
    }

    fun loadIcon(view: ImageView) {
        var iconBitmap: Bitmap? = null
        Execute({
            iconBitmap = data.loadIcon()
        }, {
            view.setImageBitmap(iconBitmap)
        })
    }
}
