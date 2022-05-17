package foundation.e.apps.utils.modules

import android.app.Activity
import android.content.Context
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import dagger.hilt.android.qualifiers.ApplicationContext
import foundation.e.apps.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeoutModule @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /*
     * Alert dialog to show to user if App Lounge times out.
     *
     * Issue: https://gitlab.e.foundation/e/backlog/-/issues/5404
     */
    private var timeoutAlertDialog: AlertDialog? = null

    /**
     * Display timeout alert dialog.
     *
     * @param activity Activity class. Basically the MainActivity.
     * @param message Alert dialog body.
     * @param positiveButtonText Positive button text. Example "Retry"
     * @param positiveButtonBlock Code block when [positiveButtonText] is pressed.
     * @param negativeButtonText Negative button text. Example "Retry"
     * @param negativeButtonBlock Code block when [negativeButtonText] is pressed.
     * @param positiveButtonText Positive button text. Example "Retry"
     * @param positiveButtonBlock Code block when [positiveButtonText] is pressed.
     *
     * Issue: https://gitlab.e.foundation/e/backlog/-/issues/5404
     * Issue: https://gitlab.e.foundation/e/backlog/-/issues/5413
     */
    fun displayTimeoutAlertDialog(
        activity: Activity,
        message: String,
        positiveButtonText: String? = null,
        positiveButtonBlock: (() -> Unit)? = null,
        negativeButtonText: String? = null,
        negativeButtonBlock: (() -> Unit)? = null,
        neutralButtonText: String? = null,
        neutralButtonBlock: (() -> Unit)? = null,
        allowCancel: Boolean = true,
    ) {
        val timeoutAlertDialogBuilder = AlertDialog.Builder(activity).apply {

            /*
             * Set title.
             */
            setTitle(R.string.timeout_title)

            if (!allowCancel) {
                /*
                 * Prevent dismissing the dialog from pressing outside as it will only
                 * show a blank screen below the dialog.
                 */
                setCancelable(false)
                /*
                 * If user presses back button to close the dialog without selecting anything,
                 * close App Lounge.
                 */
                setOnKeyListener { dialog, keyCode, _ ->
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.dismiss()
                        activity.finish()
                    }
                    true
                }
            } else {
                setCancelable(true)
            }

            /*
             * Set message
             */
            setMessage(message)

            /*
             * Set buttons.
             */
            positiveButtonText?.let {
                setPositiveButton(it) {_, _ ->
                    positiveButtonBlock?.invoke()
                }
            }
            negativeButtonText?.let {
                setNegativeButton(it) {_, _ ->
                    negativeButtonBlock?.invoke()
                }
            }
            neutralButtonText?.let {
                setNeutralButton(it) {_, _ ->
                    neutralButtonBlock?.invoke()
                }
            }
        }

        /*
         * Dismiss alert dialog if already being shown
         */
        try {
            timeoutAlertDialog?.dismiss()
        } catch (_: Exception) {}

        timeoutAlertDialog = timeoutAlertDialogBuilder.create()
        timeoutAlertDialog?.show()
    }

    /**
     * Returns true if [timeoutAlertDialog] is displaying.
     * Returs false if it is not initialised.
     */
    fun isTimeoutDialogDisplayed(): Boolean {
        return timeoutAlertDialog?.isShowing == true
    }

    /**
     * Dismisses the [timeoutAlertDialog] if it is being displayed.
     * Does nothing if it is not being displayed.
     * Caller need not check if the dialog is being displayed.
     */
    fun dismissTimeoutDialog() {
        if (isTimeoutDialogDisplayed()) {
            try {
                timeoutAlertDialog?.dismiss()
            } catch (_: Exception) {}
        }
    }

}