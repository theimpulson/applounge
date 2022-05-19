/*
 * Copyright (C) 2022  ECORP
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

package foundation.e.apps.utils.parentFragment

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.aurora.gplayapi.data.models.AuthData
import foundation.e.apps.MainActivityViewModel

/*
 * Parent class (extending fragment) for fragments which can display a timeout dialog
 * for network calls exceeding timeout limit.
 * Issue: https://gitlab.e.foundation/e/backlog/-/issues/5413
 */
abstract class TimeoutFragment(@LayoutRes layoutId: Int): Fragment(layoutId) {

    abstract fun onTimeout()

    /*
     * Set this to true when timeout dialog is once shown.
     * Set to false if user clicks "Retry".
     * Use this to prevent repeatedly showing timeout dialog.
     *
     * Setting the value to true is automatically done from TimeoutModule.displayTimeoutAlertDialog().
     * To set it as false, call resetTimeoutDialogLock().
     *
     * Timeout dialog maybe shown multiple times from MainActivity authData observer,
     * MainActivityViewModel.downloadList observer, or simply from timing out while
     * fetch the information for the fragment.
     */
    var timeoutDialogShownLock: Boolean = false

    /*
     * Do call this in the "Retry" button block of timeout dialog.
     * Also call this in onResume(), otherwise after screen off, the timeout dialog may not appear.
     */
    fun resetTimeoutDialogLock() {
        timeoutDialogShownLock = false
    }

    /*
     * Recommended to put code to refresh data inside this block.
     * But call refreshDataOrRefreshToken() to execute the refresh.
     */
    abstract fun refreshData(authData: AuthData)

    /*
     * Checks if network connectivity is present.
     * -- If yes, then checks if valid authData is present.
     * ---- If yes, then dismiss timeout dialog (if showing) and call refreshData()
     * ---- If no, then request new token data.
     */
    fun refreshDataOrRefreshToken(mainActivityViewModel: MainActivityViewModel) {
        if (mainActivityViewModel.internetConnection.value == true) {
            mainActivityViewModel.authData.value?.let { authData ->
                mainActivityViewModel.dismissTimeoutDialog()
                refreshData(authData)
            } ?: run {
                mainActivityViewModel.retryFetchingTokenAfterTimeout()
            }
        }
    }
}