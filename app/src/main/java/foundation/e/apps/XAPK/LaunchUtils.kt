package foundation.e.apps.XAPK

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

object LaunchUtils {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun startInstallSplitApksActivity(mActivity: Context, apksBean: ApksBean) {
        mActivity.startActivity(InstallSplitApksActivity.newInstanceIntent(mActivity, apksBean))
    }

}
