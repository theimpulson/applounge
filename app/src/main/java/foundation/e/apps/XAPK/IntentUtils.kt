package foundation.e.apps.XAPK

import android.content.Context
import android.content.Intent
import android.os.Build
import java.io.File

object IntentUtils {
    fun installedApk(mContext: Context, filePath: String) {
        if (FsUtils.exists(filePath)) {
            Intent().apply {
                this.action = Intent.ACTION_VIEW
                this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    this.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                this.setDataAndType(UriUtils.fromFileProvider(mContext, File(filePath)), "application/vnd.android.package-archive")
                mContext.startActivity(this)
            }
        }
    }


}
