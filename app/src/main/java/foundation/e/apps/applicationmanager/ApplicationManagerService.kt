package foundation.e.apps.applicationmanager

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger

class ApplicationManagerService : Service() {

    private val installManager = ApplicationManager()
    private val messenger = Messenger(SimpleHandler(installManager))


    override fun onCreate() {
        installManager.start(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return messenger.binder
    }

    private class SimpleHandler(private val applicationManager: ApplicationManager) : Handler() {

        @Suppress("UNCHECKED_CAST")
        override fun handleMessage(msg: Message) {
            (msg.obj as (ApplicationManager) -> Unit).invoke(applicationManager)
        }
    }
}
