package io.eelo.appinstaller

import android.content.Context
import io.eelo.appinstaller.application.InstallManager

class Settings {
    var context: Context? = null
    var APKsFolder = ""
    // TODO create the installManager when the application starts or when the phone starts (if "auto-updates" is enabled)
    val installManager: InstallManager? = null

}
