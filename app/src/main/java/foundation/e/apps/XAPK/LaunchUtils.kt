package foundation.e.apps.XAPK

import android.content.Context
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import foundation.e.apps.application.model.InstallerInterface

object LaunchUtils {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun startInstallSplitApksActivity(mActivity: Context, apksBean: ApksBean, callback: InstallerInterface) {
        mActivity.startActivity(InstallSplitApksActivity.newInstanceIntent(mActivity, apksBean))
        val handler = Handler()
        handler.postDelayed({
            callback.onInstallationComplete(mActivity)
        }, 10000)

    }

}
