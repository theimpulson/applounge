package foundation.e.apps.utils

import android.os.Build

object Common {

    /**
     * Check supported ABIs by device
     * @return An ordered list of ABIs supported by this device
     */
    fun getArchitecture(): Array<String> {
        return Build.SUPPORTED_ABIS
    }

    /**
     * Check system build type
     * @return Type of the system build, like "release" or "test"
     */
    fun getBuildType(): String {
        return Build.TAGS.split("-")[0]
    }
}