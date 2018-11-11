package io.eelo.appinstaller.utils

import android.os.AsyncTask

class Execute(private val background: () -> Unit, private val after: () -> Unit) : AsyncTask<Any, Any, Any>() {

    init {
        executeOnExecutor(Common.EXECUTOR)
    }

    override fun doInBackground(vararg params: Any?): Any? {
        background.invoke()
        return null
    }

    override fun onPostExecute(result: Any?) {
        after.invoke()
    }

}
