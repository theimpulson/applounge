package foundation.e.apps.utils.interfaces

import com.aurora.gplayapi.data.models.AuthData
import foundation.e.apps.MainActivityViewModel

/*
 * Interface for fragments which can display a timeout dialog
 * for network calls exceeding timeout limit.
 * Issue: https://gitlab.e.foundation/e/backlog/-/issues/5413
 */
interface TimeoutFragmentInterface {
    fun onTimeout()

    /*
     * Override as false!
     *
     * Set this to true when timeout dialog is once shown.
     * Set to false if user clicks "Retry".
     * Use this to prevent repeatedly showing timeout dialog.
     *
     * Setting the value to true is automatically done from TimeoutModule.displayTimeoutAlertDialog().
     * To set it as false, call resetTimeoutDialogLock() from the fragment.
     *
     * Timeout dialog maybe shown multiple times from MainActivity authData observer,
     * MainActivityViewModel.downloadList observer, or simply from timing out while
     * fetch the information for the fragment.
     */
    var timeoutDialogShownLock: Boolean
    fun resetTimeoutDialogLock() {
        timeoutDialogShownLock = false
    }

    /*
     * Recommended to put code to refresh data inside this block.
     * But call refreshDataOrRefreshToken() to execute the refresh.
     */
    fun refreshData(authData: AuthData) {}

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