package foundation.e.apps.XAPK

import foundation.e.apps.MainActivity.Companion.mActivity
import java.io.File

class XAPKFile(x: File) {

    init{
        val fileName =getSingleXApkAssetInfo(x)
        fileName?.xApkInfo?.let { it1 -> ViewUtils.installXApk(mActivity, it1) }
    }


    fun getSingleXApkAssetInfo(xApkFile: File): ApkAssetBean? {
        var apkAssetBean: ApkAssetBean? = null
        XApkInstallUtils.getXApkManifest(xApkFile)?.apply {
            val xApkManifest = this
            apkAssetBean = ApkAssetBean().apply {
                this.apkAssetType = ApkAssetType.XAPK
                this.xApkInfo = XApkInfo().apply {
                    //                    this.label = xApkManifest.getLocalLabel()
                    this.packageName = xApkManifest.packageName
                    this.appSize = xApkFile.length()
                    this.versionCode = StringUtils.parseInt(xApkManifest.versionCode) ?: 0
                    this.versionName = xApkManifest.versionName
                    this.lastModified = xApkFile.lastModified()
                    this.path = xApkFile.absolutePath
                }
                this.sortPosition = this.xApkInfo?.lastModified ?: 0L
            }
        }
        return apkAssetBean
    }
}
