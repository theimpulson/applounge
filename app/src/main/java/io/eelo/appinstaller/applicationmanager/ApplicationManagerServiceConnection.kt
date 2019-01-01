package io.eelo.appinstaller.applicationmanager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Message
import android.os.Messenger

class ApplicationManagerServiceConnection : ServiceConnection {

    private lateinit var applicationManager: ApplicationManager
    private val blocker = Object()

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Messenger(service).send(Message.obtain(null, 0, { result: ApplicationManager ->
            applicationManager = result
            synchronized(blocker) {
                blocker.notify()
            }
        }))
    }

    override fun onServiceDisconnected(name: ComponentName?) {
    }

    fun connectAndGet(context: Context): ApplicationManager {
        context.startService(Intent(context, ApplicationManagerService::class.java))
        context.bindService(Intent(context, ApplicationManagerService::class.java), this, Context.BIND_AUTO_CREATE)
        synchronized(blocker) {
            blocker.wait()
        }
        return applicationManager
    }

    fun disconnect(context: Context) {
        context.unbindService(this)
    }

}
