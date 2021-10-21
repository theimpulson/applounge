package foundation.e.apps

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import foundation.e.apps.utils.pkg.PkgManagerBR
import foundation.e.apps.utils.pkg.PkgManagerModule
import javax.inject.Inject

@HiltAndroidApp
class AppsApplication : Application() {

    @Inject
    lateinit var pkgManagerModule: PkgManagerModule

    override fun onCreate() {
        super.onCreate()

        // Register broadcast receiver for package manager
        val pkgManagerBR = object : PkgManagerBR() {}
        registerReceiver(pkgManagerBR, pkgManagerModule.getFilter())
    }
}
