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

package foundation.e.apps.application.model

import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Execute
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
            Execute({}, {
                action()
            })
            waiter.wait()
        }
    }

    private fun action() {
        action.invoke()
        synchronized(waiter) {
            waiter.notifyAll()
        }
    }
}
