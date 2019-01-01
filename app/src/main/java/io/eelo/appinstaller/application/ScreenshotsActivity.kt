package io.eelo.appinstaller.application

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManagerGetter
import io.eelo.appinstaller.utils.Constants
import io.eelo.appinstaller.utils.Constants.SELECTED_APPLICATION_SCREENSHOT_KEY
import io.eelo.appinstaller.utils.Execute
import kotlinx.android.synthetic.main.activity_screenshots.*

class ScreenshotsActivity : AppCompatActivity() {
    private val installManagerGetter = InstallManagerGetter()
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
            initialise(applicationPackageName!!)
        }
    }

    private fun initialise(packageName: String) {
        Execute({
            val installManager = installManagerGetter.connectAndGet(this)
            application = installManager.findOrCreateApp(packageName)
        }, {
            onApplicationInfoLoaded()
        })
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
        installManagerGetter.disconnect(this)
    }
}
