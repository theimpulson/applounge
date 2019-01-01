package io.eelo.appinstaller.applicationmanager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Message
import android.os.Messenger

class ApplicationManagerServiceConnection(
        private val callback: ApplicationManagerServiceConnectionCallback) : ServiceConnection {

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Messenger(service).send(Message.obtain(null, 0, { result: ApplicationManager ->
            callback.onServiceBind(result)
        }))
    }

    override fun onServiceDisconnected(name: ComponentName?) {
    }

    fun bindService(context: Context) {
        context.startService(Intent(context, ApplicationManagerService::class.java))
        context.bindService(Intent(context, ApplicationManagerService::class.java), this, Context.BIND_AUTO_CREATE)
    }

    fun unbindService(context: Context) {
        context.unbindService(this)
    }

}
