package foundation.e.apps.utils

import android.os.Build
import android.util.Log

class OsInfo() {

    fun getOSReleaseType(): String {
        val buildTags = Build.TAGS.split(",").toTypedArray()
        var osReleaseType = ""
        buildTags.forEach {
            if (it.contains("-release")) {
                osReleaseType = it.substringBefore("-release")
            }
        }

        Log.i("foundation.e.apps", "Release Type: $osReleaseType")

        return osReleaseType
    }

}