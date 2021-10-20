package foundation.e.apps.api.fused

import foundation.e.apps.api.data.Origin

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