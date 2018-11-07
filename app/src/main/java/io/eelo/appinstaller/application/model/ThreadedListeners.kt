package io.eelo.appinstaller.application.model

import android.os.AsyncTask
import io.eelo.appinstaller.utils.Common
import java.util.concurrent.atomic.AtomicBoolean

class ThreadedListeners(private val action: () -> Unit) {

    private val stopped = AtomicBoolean(false)
    private val waiter = Object()

    fun start() {
        Common.EXECUTOR.submit(this::run)
    }

    fun stop() {
        stopped.set(true)
        synchronized(waiter) {
            waiter.wait()
        }
    }

    private fun run() {
        while (!stopped.get()) {
            execSynchronized()
        }
    }

    private fun execSynchronized() {
        synchronized(waiter) {
            SyncTask(this).executeOnExecutor(Common.EXECUTOR)
            waiter.wait()
        }
    }

    private fun action() {
        action.invoke()
        synchronized(waiter) {
            waiter.notifyAll()
        }
    }

    private class SyncTask(private val threadedListeners: ThreadedListeners) : AsyncTask<Void?, Void?, Void?>() {

        override fun doInBackground(vararg voids: Void?): Void? {
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            threadedListeners.action()
        }
    }

}
