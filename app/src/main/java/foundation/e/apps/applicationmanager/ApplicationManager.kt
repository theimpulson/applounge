/*
 * Copyright (C) 2019-2021  E FOUNDATION
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.applicationmanager

import android.content.Context
import foundation.e.apps.application.model.Application
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class ApplicationManager {

    private val apps = HashMap<String, Application>()
    private val queue = LinkedBlockingQueue<Application>()

    @Synchronized
    fun findOrCreateApp(packageName: String?): Application {
        if (!apps.containsKey(packageName)) {
            apps[packageName!!] = Application(packageName, this)
        }
        val app = apps[packageName]!!
        app.incrementUses()
        return app
    }

    @Synchronized
    fun install(context: Context, app: Application) {
        if (!queue.contains(app)) {
            queue.put(app)
            queue.put(app)
        }
        app.checkForStateUpdate(context)
    }

    fun start(context: Context) {
        Thread {
            startInstalls(context)
        }.start()
    }

    private fun startInstalls(context: Context) {
        while (true) {
            val app = queue.take()
            app.download(context)
            stopInstalling(context, app)
            tryRemove(app)
        }
    }

    fun tryRemove(app: Application) {
        if (!app.isUsed() && !queue.contains(app)) {
            apps.remove(app.packageName)
        }
    }

    @Synchronized
    fun stopInstalling(context: Context, app: Application) {
        while (queue.remove(app)) {
        }
        app.checkForStateUpdate(context)
    }

    fun isInstalling(app: Application): Boolean {
        return queue.contains(app)
    }
}
