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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Message
import android.os.Messenger

class ApplicationManagerServiceConnection(
    private val callback: ApplicationManagerServiceConnectionCallback
) : ServiceConnection {

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Messenger(service).send(
            Message.obtain(
                null, 0,
                { result: ApplicationManager ->
                    callback.onServiceBind(result)
                }
            )
        )
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