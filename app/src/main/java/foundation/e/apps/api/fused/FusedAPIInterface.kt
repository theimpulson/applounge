package foundation.e.apps.api.fused

import foundation.e.apps.api.data.Origin

/**
 * FusedAPIInterface to allow adapter classes to install applications easily
 */
interface FusedAPIInterface {

    fun getApplication(
        id: String,
        name: String,
        packageName: String,
        versionCode: Int,
        offerType: Int?,
        origin: Origin?
    )
}
