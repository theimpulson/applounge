package foundation.e.apps.XAPK

import android.app.ProgressDialog
import android.content.Context
import android.os.Build
import androidx.annotation.MainThread
import foundation.e.apps.R
import foundation.e.apps.application.model.InstallerInterface
import java.io.File


object ViewUtils {

    @MainThread
    fun installXApk(mContext: Context, xApkInfo: XApkInfo, callback: InstallerInterface) {
        XApkInstallUtils.installXApk(File(xApkInfo.path),callback, object : XApkInstallUtils.XApkInstallProgressCallback {
            private var progressDialog: ProgressDialog? = null
            override fun onStart() {
                // progress dialog if needed
            }

            override fun onObbProgress(currentOffset: Long, totalLength: Long, percent: Int) {
                progressDialog?.apply {
                    if (this.isShowing) {
                        this.setMessage(mContext.getString(R.string.state_installing))
                        if (percent > 0) {
                            this.isIndeterminate = false
                            this.progress = percent
                        } else {
                            this.isIndeterminate = true
                        }
                    }
                }
            }

            override fun onApkProgress(currentOffset: Long, totalLength: Long, percent: Int) {
            //progress dialog if needed
            }

            override fun onCompedApk(apkFile: File) {
                progressDialog?.apply {
                    if (this.isShowing) {
                        this.dismiss()
                    }
                }
                IntentUtils.installedApk(mContext, apkFile.absolutePath)
            }

            override fun onCompedApks(apksBean: ApksBean, callback: InstallerInterface) {
                progressDialog?.apply {
                    if (this.isShowing) {
                        this.dismiss()
                    }
                }
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> LaunchUtils.startInstallSplitApksActivity(
                            mContext,
                            apksBean,callback
                    )
                    apksBean.splitApkPaths?.size == 1 -> IntentUtils.installedApk(mContext, apksBean.splitApkPaths!![0])
                    else -> onError(XApkInstallUtils.InstallError.LowerSdkError)
                }
            }

            override fun onError(installError: XApkInstallUtils.InstallError) {
                progressDialog?.apply {
                    if (this.isShowing) {
                        this.dismiss()
                    }
                }
//                when (installError) {
//                    XApkInstallUtils.InstallError.ObbError -> SimpleToast.defaultShow(mContext, R.string.install_obb_failed)
//                    XApkInstallUtils.InstallError.LowerVersionError -> SimpleToast.defaultShow(mContext, R.string.xapk_lower_version_error)
//                    XApkInstallUtils.InstallError.LowerSdkError -> SimpleToast.defaultShow(mContext, R.string.part_xapk_sdk_lower_version_error)
//                    else -> SimpleToast.defaultShow(mContext, R.string.install_failed)
//                }
            }
        })
    }

}