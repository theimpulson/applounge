package io.eelo.appinstaller.application.model

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Message
import android.os.Messenger

class InstallManagerGetter : ServiceConnection {

    private lateinit var installManager: InstallManager
    private val blocker = Object()

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Messenger(service).send(Message.obtain(null, 0, { result: InstallManager ->
            installManager = result
            synchronized(blocker) {
                blocker.notify()
            }
        }))
    }

    override fun onServiceDisconnected(name: ComponentName?) {
    }

    fun connectAndGet(context: Context): InstallManager {
        context.startService(Intent(context, InstallManagerService::class.java))
        val blocker = Object()
        context.bindService(Intent(context, InstallManagerService::class.java), this, Context.BIND_AUTO_CREATE)
        synchronized(blocker) {
            blocker.wait()
        }
        return installManager
    }

    fun disconnect(context: Context) {
        context.unbindService(this)
    }

}
