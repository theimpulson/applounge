package foundation.e.apps.application

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import foundation.e.apps.R
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.applicationmanager.ApplicationManagerServiceConnection
import foundation.e.apps.applicationmanager.ApplicationManagerServiceConnectionCallback
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Constants.SELECTED_APPLICATION_SCREENSHOT_KEY
import kotlinx.android.synthetic.main.activity_screenshots.*

class ScreenshotsActivity : AppCompatActivity(), ApplicationManagerServiceConnectionCallback {
    private val applicationManagerServiceConnection =
            ApplicationManagerServiceConnection(this)
    private lateinit var applicationPackageName: String
    private lateinit var application: Application
    private lateinit var screenshotsCarousel: ViewPager
    private var lastSelectedScreenshotIndex = 0
    private val last_selected_screenshot_key = "last_selected_screenshot"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screenshots)

        if (savedInstanceState != null &&
                savedInstanceState.containsKey(last_selected_screenshot_key)) {
            lastSelectedScreenshotIndex = savedInstanceState.getInt(last_selected_screenshot_key)

        } else if (intent.hasExtra(SELECTED_APPLICATION_SCREENSHOT_KEY)) {
            lastSelectedScreenshotIndex =
                    intent.getIntExtra(SELECTED_APPLICATION_SCREENSHOT_KEY, 0)
        }

        val applicationPackageName: String? =
                intent.getStringExtra(Constants.APPLICATION_PACKAGE_NAME_KEY)
        if (!applicationPackageName.isNullOrEmpty()) {
            this.applicationPackageName = applicationPackageName!!
            applicationManagerServiceConnection.bindService(this)
        }
    }

    override fun onServiceBind(applicationManager: ApplicationManager) {
        application = applicationManager.findOrCreateApp(applicationPackageName)
        onApplicationInfoLoaded()
    }

    private fun onApplicationInfoLoaded() {
        val basicData = application.basicData!!

        screenshotsCarousel = screenshots_carousel
        screenshotsCarousel.visibility = View.GONE

        basicData.loadImagesAsyncly {
            if (it.isNotEmpty()) {
                screenshotsCarousel.adapter = ScreenshotsCarouselAdapter(this, it)
                screenshotsCarousel.setCurrentItem(lastSelectedScreenshotIndex, false)
                screenshotsCarousel.visibility = View.VISIBLE
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (::screenshotsCarousel.isInitialized) {
            outState?.putInt(last_selected_screenshot_key, screenshotsCarousel.currentItem)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        application.decrementUses()
        applicationManagerServiceConnection.unbindService(this)
    }
}
