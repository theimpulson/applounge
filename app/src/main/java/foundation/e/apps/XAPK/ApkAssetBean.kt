package foundation.e.apps.XAPK

data class ApkAssetBean(
                        var xApkInfo: XApkInfo?,
                        var sortPosition: Long,
                        var apkAssetType: ApkAssetType) {
    constructor() : this( null,  0L, ApkAssetType.XAPK)
}
