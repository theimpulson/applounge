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