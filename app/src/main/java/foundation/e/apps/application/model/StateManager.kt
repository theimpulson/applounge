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

import android.content.Context
import foundation.e.apps.application.model.data.BasicData
import foundation.e.apps.application.model.data.SearchAppsBasicData
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Constants.MICROG_SHARED_PREF
import foundation.e.apps.utils.Error
import foundation.e.apps.utils.PreferenceStorage
import java.util.*

class StateManager(private val info: ApplicationInfo, private val app: Application, private val appManager: ApplicationManager) {
    private var listeners = Collections.synchronizedList(ArrayList<ApplicationStateListener>())

    var state = State.NOT_DOWNLOADED
        private set

    fun find(context: Context, basicData: BasicData) {
        if (basicData.name == Constants.MICROG) {
            Common.updateMicroGStatus(context)
            val state = if (appManager.isInstalling(app) && !app.isInstalling) {
                State.DOWNLOADING
            } else if (appManager.isInstalling(app) && app.isInstalling) {
                State.INSTALLING
            } else if (PreferenceStorage(context).getBoolean(MICROG_SHARED_PREF, false)) {
                if (info.isLastVersionInstalled(context, basicData.lastVersionNumber)) {
                    State.NOT_UPDATED
                } else {
                    State.INSTALLED
                }
            } else {
                State.NOT_DOWNLOADED // not installed
            }
            changeState(state)
        } else {
            val state = if (appManager.isInstalling(app) && !app.isInstalling) {
                State.DOWNLOADING
            } else if (appManager.isInstalling(app) && app.isInstalling) {
                State.INSTALLING
            } else if (info.isLastVersionInstalled(context,
                            basicData.getLastVersion() ?: "")) {
                State.INSTALLED
            } else if (info.isInstalled(context) && !info.isLastVersionInstalled(context,
                            basicData.getLastVersion() ?: "")) {
                State.NOT_UPDATED
            } else {
                State.NOT_DOWNLOADED
            }
            changeState(state)
        }
    }

    fun findSystemApp(context: Context, basicData: BasicData) {
        val state = if (appManager.isInstalling(app) && !app.isInstalling) {
            State.DOWNLOADING
        } else if (appManager.isInstalling(app) && app.isInstalling) {
            State.INSTALLING
        } else if (PreferenceStorage(context).getBoolean(MICROG_SHARED_PREF, false)) {
            if (info.isLastVersionInstalled(context, basicData.lastVersionNumber)) {
                State.NOT_UPDATED
            } else {
                State.INSTALLED
            }
        } else {
            State.NOT_DOWNLOADED
        }
        changeState(state)
    }

    fun searchAppsFind(context: Context, basicData: SearchAppsBasicData) {
        val state = if (appManager.isInstalling(app) && !app.isInstalling) {
            State.DOWNLOADING
        } else if (appManager.isInstalling(app) && app.isInstalling) {
            State.INSTALLING
        } else if (info.isLastVersionInstalled(context,
                        basicData.getLastVersion() ?: "")) {
            State.INSTALLED
        } else if (info.isInstalled(context) && !info.isLastVersionInstalled(context,
                        basicData.getLastVersion() ?: "")) {
            State.NOT_UPDATED
        } else {
            State.NOT_DOWNLOADED
        }
        changeState(state)
    }

    fun pwaFind() {
        val state = if (appManager.isInstalling(app) && !app.isInstalling) {
            State.DOWNLOADING
        } else if (appManager.isInstalling(app) && app.isInstalling) {
            State.INSTALLING
        } else {
            State.NOT_DOWNLOADED
        }
        changeState(state)
    }

    private fun changeState(state: State) {
        this.state = state
        notifyStateChange()
    }

    fun addListener(listener: ApplicationStateListener) {
        listeners.add(listener)
        if (state == State.DOWNLOADING) {
            app.downloader?.let { listener.downloading(it) }
        }
    }

    fun removeListener(listener: ApplicationStateListener) {
        listeners.remove(listener)
    }

    fun notifyDownloading(downloader: Downloader) {
        listeners.forEach { listener: ApplicationStateListener ->
            listener.downloading(downloader)
        }
    }

    fun notifyError(error: Error) {
        listeners.forEach { listener: ApplicationStateListener ->
            listener.anErrorHasOccurred(error)
        }
    }

    private fun notifyStateChange() {
        listeners.forEach { listener: ApplicationStateListener ->
            listener.stateChanged(state)
        }
    }
}
