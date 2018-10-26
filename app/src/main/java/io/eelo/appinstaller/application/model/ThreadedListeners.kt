package io.eelo.appinstaller.application.model

import android.os.AsyncTask

import java.util.concurrent.atomic.AtomicBoolean

class ThreadedListeners(private val action: () -> Unit) {

    private val stopped = AtomicBoolean(false)
    private val waiter = Object()

    fun start() {
        Thread(this::run).start()
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
            SyncTask(this).execute()
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
