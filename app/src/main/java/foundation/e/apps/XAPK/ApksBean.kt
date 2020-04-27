package foundation.e.apps.XAPK

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
 data class ApksBean(
    var packageName: String,
    var label: String,
    var iconPath: String,
    var apkAssetType: ApkAssetType?,
    var outputFileDir: String,
    var splitApkPaths: ArrayList<String>?
) : Parcelable {

    constructor() : this(String(), String(), String(), null, String(), null)
}


