/*
 * Copyright (C) 2019-2021  E FOUNDATION
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.application

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.applicationmanager.ApplicationManagerServiceConnection
import foundation.e.apps.applicationmanager.ApplicationManagerServiceConnectionCallback
import foundation.e.apps.databinding.ActivityScreenshotsBinding
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Constants.SELECTED_APPLICATION_SCREENSHOT_KEY

class ScreenshotsActivity : AppCompatActivity(), ApplicationManagerServiceConnectionCallback {
    private lateinit var binding: ActivityScreenshotsBinding

    private val applicationManagerServiceConnection =
        ApplicationManagerServiceConnection(this)
    private lateinit var applicationPackageName: String
    private lateinit var application: Application
    private lateinit var screenshotsCarousel: ViewPager
    private var lastSelectedScreenshotIndex = 0
    private val last_selected_screenshot_key = "last_selected_screenshot"

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityScreenshotsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState != null &&
            savedInstanceState.containsKey(last_selected_screenshot_key)
        ) {
            lastSelectedScreenshotIndex = savedInstanceState.getInt(last_selected_screenshot_key)
        } else if (intent.hasExtra(SELECTED_APPLICATION_SCREENSHOT_KEY)) {
            lastSelectedScreenshotIndex =
                intent.getIntExtra(SELECTED_APPLICATION_SCREENSHOT_KEY, 0)
        }

        val applicationPackageName: String? =
            intent.getStringExtra(Constants.APPLICATION_PACKAGE_NAME_KEY)
        if (!applicationPackageName.isNullOrEmpty()) {
            this.applicationPackageName = applicationPackageName
            applicationManagerServiceConnection.bindService(this)
        }
    }

    override fun onServiceBind(applicationManager: ApplicationManager) {
        application = applicationManager.findOrCreateApp(applicationPackageName)
        onApplicationInfoLoaded()
    }

    private fun onApplicationInfoLoaded() {

        val basicData = application.basicData
        val pwasBasicData = application.pwabasicdata

        if (pwasBasicData != null) {
            screenshotsCarousel = binding.screenshotsCarousel
            screenshotsCarousel.visibility = View.GONE

            pwasBasicData.loadImagesAsyncly {
                if (it.isNotEmpty()) {
                    screenshotsCarousel.adapter = ScreenshotsCarouselAdapter(this, it)
                    screenshotsCarousel.setCurrentItem(lastSelectedScreenshotIndex, false)
                    screenshotsCarousel.visibility = View.VISIBLE
                }
            }
        } else {
            screenshotsCarousel = binding.screenshotsCarousel
            screenshotsCarousel.visibility = View.GONE

            basicData!!.loadImagesAsyncly {
                if (it.isNotEmpty()) {
                    screenshotsCarousel.adapter = ScreenshotsCarouselAdapter(this, it)
                    screenshotsCarousel.setCurrentItem(lastSelectedScreenshotIndex, false)
                    screenshotsCarousel.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::screenshotsCarousel.isInitialized) {
            outState.putInt(last_selected_screenshot_key, screenshotsCarousel.currentItem)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        application.decrementUses()
        applicationManagerServiceConnection.unbindService(this)
    }
}
