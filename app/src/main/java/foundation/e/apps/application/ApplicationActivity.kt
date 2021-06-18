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

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import foundation.e.apps.MainActivity.Companion.sharedPreferences
import foundation.e.apps.R
import foundation.e.apps.application.model.Application
import foundation.e.apps.application.model.ApplicationStateListener
import foundation.e.apps.application.model.Downloader
import foundation.e.apps.application.model.State
import foundation.e.apps.application.model.data.BasicData
import foundation.e.apps.application.model.data.FullData
import foundation.e.apps.application.model.data.PwaFullData
import foundation.e.apps.application.model.data.PwasBasicData
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.applicationmanager.ApplicationManagerServiceConnection
import foundation.e.apps.applicationmanager.ApplicationManagerServiceConnectionCallback
import foundation.e.apps.categories.category.CategoryActivity
import foundation.e.apps.databinding.ActivityApplicationBinding
import foundation.e.apps.pwa.PwaInstaller
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Common.toMiB
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Constants.APPLICATION_DESCRIPTION_KEY
import foundation.e.apps.utils.Constants.APPLICATION_PACKAGE_NAME_KEY
import foundation.e.apps.utils.Constants.SELECTED_APPLICATION_SCREENSHOT_KEY
import foundation.e.apps.utils.Error
import foundation.e.apps.utils.Execute
import kotlin.math.roundToInt

class ApplicationActivity :
    AppCompatActivity(),
    ApplicationStateListener,
    ApplicationManagerServiceConnectionCallback,
    Downloader.DownloadProgressCallback,
    BasicData.IconLoaderCallback,
    PwasBasicData.IconLoaderCallback {

    private lateinit var binding: ActivityApplicationBinding

    private lateinit var applicationPackageName: String
    private lateinit var application: Application
    private val applicationManagerServiceConnection =
        ApplicationManagerServiceConnection(this)
    private var imageWidth = 0
    private var imageHeight = 0
    private var imageMargin = 0
    private var defaultElevation = 0f
    private val sharedPrefFile = "kotlinsharedpreference"

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityApplicationBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val accentColorOS = Common.getAccentColor(this)

        binding.goodBorder.visibility = View.GONE
        binding.neutralBorder.visibility = View.GONE


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        sharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        binding.pwaSympol.visibility = View.GONE

        initialiseDimensions()
        val applicationPackageName: String? = intent.getStringExtra(APPLICATION_PACKAGE_NAME_KEY)
        if (!applicationPackageName.isNullOrEmpty()) {
            this.applicationPackageName = applicationPackageName
            applicationManagerServiceConnection.bindService(this)
        }

        // Set accent color
        binding.appDownloadProgress.progressDrawable.colorFilter = PorterDuffColorFilter(accentColorOS, PorterDuff.Mode.SRC_IN)
        binding.appCategory.setTextColor(accentColorOS)
        binding.appExpandDescription.setTextColor(accentColorOS)


    }


    private fun initialiseElevation() {
        if (binding.scrollView.scrollY == 0) {
            binding.toolbar.elevation = 0f
        } else {
            binding.toolbar.elevation = defaultElevation
        }
        binding.scrollView.setOnScrollChangeListener { view, _, _, _, _ ->
            if (view.scrollY == 0) {
                binding.toolbar.elevation = 0f
            } else {
                binding.toolbar.elevation = defaultElevation
            }
        }
    }

    override fun onServiceBind(applicationManager: ApplicationManager) {
        application = applicationManager.findOrCreateApp(applicationPackageName)
        var error: Error? = null
        Execute({
                    error = application.assertFullData(this)
                }, {
                    if (error == null) {

                        onApplicationInfoLoaded()
                    } else {
                        Snackbar.make(binding.container,
                                      getString(error!!.description),
                                      Snackbar.LENGTH_LONG).show()

                        // Close activity once snackbar has hidden
                        object : CountDownTimer(3500, 3500) {
                            override fun onTick(p0: Long) {
                                // Do nothing
                            }

                            override fun onFinish() {
                                finish()
                            }
                        }.start()
                    }
                })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_application_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            else -> {
                return false
            }
        }
        return true
    }

    private fun initialiseDimensions() {
        // Do some math and figure out item width, padding and margin
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val logicalDensity = metrics.density

        imageWidth = Math.ceil(120 * logicalDensity.toDouble()).roundToInt()
        imageHeight = Math.ceil(210 * logicalDensity.toDouble()).roundToInt()
        imageMargin = Math.ceil(4 * logicalDensity.toDouble()).roundToInt()
        defaultElevation = Math.ceil(resources.getDimension(R.dimen.default_elevation) * logicalDensity.toDouble()).toFloat()
    }

    @SuppressLint("ResourceAsColor")
    private fun textColorChange(text: String): SpannableStringBuilder {
        val builder = SpannableStringBuilder();
        val redSpannable = SpannableString(text);
        redSpannable.setSpan(ForegroundColorSpan(R.color.colorTextSecondary), 0, text.length, 0);
        builder.append(redSpannable);
        return builder
    }

    private fun onApplicationInfoLoaded() {
        initialiseElevation()

        if (application.basicData != null) {

            val basicData = application.basicData!!
            val fullData = application.fullData!!

            // Load the app icon
            application.loadIcon(this)


            // Set the app title
            if (basicData.name.isNotEmpty()) {
                binding.appTitle.text = basicData.name
            } else {
                binding.appTitle.visibility = View.GONE
            }


            // Set the app author
            if (basicData.author.isNotEmpty()) {
                binding.appAuthor.text = basicData.author
            } else {
                binding.appAuthor.visibility = View.GONE
            }

            // Set the app category
            if (fullData.category.getTitle().isNotEmpty()) {
                binding.appCategory.text = fullData.category.getTitle()
                binding.appCategory.setOnClickListener {
                    startActivity(Intent(this, CategoryActivity::class.java).apply {
                        putExtra(Constants.CATEGORY_KEY, fullData.category)
                    })
                }
            } else {
                binding.appCategory.visibility = View.GONE
            }

            // Set the app description
            if (fullData.description.isNotEmpty()) {
                binding.appDescription.text = fullData.description
                binding.appDescriptionContainer.isEnabled = true
            } else {
                binding.appDescription.text = getString(R.string.not_available_full)
                binding.appDescriptionContainer.isEnabled = false
            }

            // Handle clicks on description
            binding.appDescriptionContainer.setOnClickListener {
                val intent = Intent(this, ApplicationDescriptionActivity::class.java)
                intent.putExtra(APPLICATION_DESCRIPTION_KEY, application.fullData!!.description)
                startActivity(intent)
            }

            // Set the app rating
            if (basicData.ratings.rating != -1f) {
                binding.appRating.text = basicData.ratings.rating.toString() + "/5"
            } else {
                binding.appRating.text = textColorChange(getText(R.string.not_available).toString())
            }
            setRatingBorder(basicData.ratings.rating)

            binding.appRatingContainer.setOnClickListener {
                val alertDialog = AlertDialog.Builder(this).create()

                alertDialog.setIcon(R.drawable.ic_app_rating)
                alertDialog.setTitle(R.string.app_rating)
                alertDialog.setMessage(getString(R.string.app_rating_description))
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok))
                { _, _ ->
                    alertDialog.dismiss()
                }
                alertDialog.show()
                var b = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                b.setTextColor(Color.parseColor("#0088ED"))
            }

            // Load the app screenshots
            basicData.loadImagesAsyncly {
                showImages(it)
            }

            //Set the app licence
            if (fullData.licence.isNotEmpty()) {
                binding.appLicence.text = fullData.licence
            } else {
                binding.appLicence.text = getString(R.string.not_available)
            }

            if (fullData.getLastVersion() != null) {
                // Set app size
                if (fullData.getLastVersion()!!.fileSize.isNotEmpty()) {
                    binding.appSize.text = fullData.getLastVersion()!!.fileSize
                } else {
                    binding.appSize.visibility = View.GONE
                }

                // Set the app privacy rating
                if (fullData.getLastVersion()!!.privacyRating != null &&
                    fullData.getLastVersion()!!.privacyRating != -1) {
                    binding.appPrivacyScore.text = fullData.getLastVersion()!!.privacyRating.toString() + "/10"
                    setPrivacyRatingBorder(fullData.getLastVersion()!!.privacyRating!!)
                } else {
                    binding.appPrivacyScore.text = textColorChange(getText(R.string.not_available).toString())
                    setPrivacyRatingBorder(-1)
                }
                binding.appPrivacyContainer.setOnClickListener {
                    val message = layoutInflater.inflate(R.layout.privacy_dialog_message, null) as
                            TextView

                    @Suppress("DEPRECATION")
                    message.setText((Html.fromHtml("Score out of 10. Computed using <a href=\'https://exodus-privacy.eu.org\'>Exodus Privacy analyses</a>, based on permissions and trackers used in the app")))
                    message.setLinkTextColor(Color.parseColor("#0088ED"))
                    message.setMovementMethod(LinkMovementMethod.getInstance())

                    message.movementMethod = (LinkMovementMethod.getInstance())
                    val alertDialog = AlertDialog.Builder(this).create()
                    alertDialog.setIcon(R.drawable.ic_dialog_info)
                    alertDialog.setTitle(R.string.app_privacy_score)
                    alertDialog.setView(message)
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok))
                    { _, _ ->
                        alertDialog.dismiss()
                    }
                    alertDialog.show()
                    var b = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    b.setTextColor(Color.parseColor("#0088ED"))
                }

                // Set app version
                if (fullData.getLastVersion()!!.version.isNotEmpty()) {
                    binding.appVersion.text = fullData.getLastVersion()!!.version
                } else {
                    binding.appVersion.text = getString(R.string.not_available)
                }
                // Set app package name.
                if (fullData.packageName.isNotEmpty()) {
                    binding.appPackageName.text = fullData.packageName
                } else {
                    binding.appPackageName.text = getString(R.string.not_available)
                }

                // Set app update timestamp
                if (fullData.getLastVersion()!!.createdOn.isNotEmpty()) {
                    binding.appUpdatedOn.text = getFormattedTimestamp(fullData.getLastVersion()!!.createdOn)
                } else {
                    binding.appUpdatedOn.text = getString(R.string.not_available)
                }

                // Set app minimum required Android version
                if (fullData.getLastVersion()!!.minAndroid.isNotEmpty()) {
                    binding.appMinAndroid.text =
                        getFormattedMinSdkVersion(fullData.getLastVersion()!!.minAndroid)
                } else {
                    binding.appMinAndroid.text = getString(R.string.not_available)
                }
            } else {
                // Set app size
                binding.appSize.visibility = View.GONE

                // Set app privacy rating
                binding.appPrivacyScore.text = textColorChange(getText(R.string.not_available).toString())
                setPrivacyRatingBorder(-1)
                binding.appPrivacyContainer.setOnClickListener {
                    val alertDialog = AlertDialog.Builder(this).create()
                    alertDialog.setIcon(R.drawable.ic_dialog_info)
                    alertDialog.setTitle(R.string.app_privacy_score)
                    alertDialog.setMessage(getString(R.string.app_privacy_description))
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok))
                    { _, _ ->
                        alertDialog.dismiss()
                    }
                    alertDialog.show()
                    var b = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    b.setTextColor(Color.parseColor("#0088ED"))
                }

                // Set app version
                binding.appVersion.text = getString(R.string.not_available_full)

                // Set app update timestamp
                binding.appUpdatedOn.text = getString(R.string.not_available_full)

                // Set app minimum required Android version
                binding.appMinAndroid.text = getString(R.string.not_available_full)
            }

            // Handle clicks on app permissions
            binding.appPermissionsContainer.setOnClickListener {
                val layout = layoutInflater.inflate(R.layout.custom_alert_dialog_layout, null)
                val message = layout.findViewById<TextView>(R.id.message)
                val alertDialog = AlertDialog.Builder(this).create()
                alertDialog.setTitle(R.string.app_permissions_title)
                if (fullData.getLastVersion() != null &&
                    fullData.getLastVersion()!!.exodusPermissions != null) {
                    if (fullData.getLastVersion()!!.exodusPermissions!!.isNotEmpty()) {
                        var rawMessage = ""
                        var index = 0
                        fullData.getLastVersion()!!.exodusPermissions!!.forEach { permission ->
                            rawMessage += permission
                            if (index != fullData.getLastVersion()!!.exodusPermissions!!.size - 1) {
                                rawMessage += "\n"
                            }
                            index++
                        }
                        message.text = rawMessage
                    } else {
                        message.text = getString(R.string.no_permissions)
                    }
                } else {
                    message.text = getString(R.string.not_available_full)
                }
                alertDialog.setView(layout)
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok))
                { _, _ ->
                    alertDialog.dismiss()
                }
                alertDialog.show()
                var b = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                b.setTextColor(Color.parseColor("#0088ED"))
                layout.findViewById<TextView>(R.id.privacy_message).movementMethod = LinkMovementMethod.getInstance()
            }

            // Handle clicks on app trackers
            binding.appTrackersContainer.setOnClickListener {
                val layout = layoutInflater.inflate(R.layout.custom_alert_dialog_layout, null)
                val message = layout.findViewById<TextView>(R.id.message)

                val linkMessage = layout.findViewById<TextView>(R.id.privacy_message)
                @Suppress("DEPRECATION")
                linkMessage.setText((Html.fromHtml("Computed using <a href=\'https://exodus-privacy.eu.org\'>Exodus Privacy analyses</a>.")))
                linkMessage.setLinkTextColor(Color.parseColor("#0088ED"))
                linkMessage.setMovementMethod(LinkMovementMethod.getInstance())

                val alertDialog = AlertDialog.Builder(this).create()

                alertDialog.setTitle(R.string.app_trackers_title)
                if (fullData.getLastVersion() != null &&
                    fullData.getLastVersion()!!.exodusTrackers != null) {
                    if (fullData.getLastVersion()!!.exodusTrackers!!.isNotEmpty()) {
                        var rawMessage = ""
                        var index = 0
                        fullData.getLastVersion()!!.exodusTrackers!!.forEach { tracker ->
                            rawMessage += tracker
                            if (index != fullData.getLastVersion()!!.exodusTrackers!!.size - 1) {
                                rawMessage += "\n"
                            }
                            index++
                        }
                        message.text = rawMessage
                    } else {
                        message.text = getString(R.string.no_trackers)
                    }
                } else {
                    message.text = getString(R.string.not_available_full)
                }
                alertDialog.setView(layout)
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok))
                { _, _ ->
                    alertDialog.dismiss()
                }
                alertDialog.show()
                var b = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                b.setTextColor(Color.parseColor("#0088ED"))
                linkMessage.movementMethod = LinkMovementMethod.getInstance()
            }

            application.addListener(this)
            stateChanged(application.state)

            // Handle clicks on app install button
            binding.installButtonLayout.appInstall.setOnClickListener {
                onInstallButtonClick(fullData)
            }
        } else {
            onPwaApplicationLoaded()
        }

    }

    @SuppressLint("NewApi")
    private fun onPwaApplicationLoaded() {

        application.PwaloadIcon(this)
        binding.pwaSympol.visibility = View.VISIBLE
        binding.Ratings.visibility = View.GONE


        val pwasBasicData = application.pwabasicdata
        val pwaFullData = application.pwaFullData


        // Set the app title
        if (pwasBasicData!!.name.isNotEmpty()) {
            binding.appTitle.text = pwasBasicData.name
        } else {
            binding.appTitle.visibility = View.GONE
        }

        // Set the app description
        if (pwaFullData!!.description.isNotEmpty()) {
            binding.appDescription.text = pwaFullData.description
            binding.appDescriptionContainer.isEnabled = true
        } else {
            binding.appDescription.text = getString(R.string.not_available_full)
            binding.appDescriptionContainer.isEnabled = false
        }

        if (pwaFullData.category.getTitle().isNotEmpty()) {
            binding.appCategory.text = pwaFullData.category.getTitle()
            binding.appCategory.setOnClickListener {
                startActivity(Intent(this, CategoryActivity::class.java).apply {
                    putExtra(Constants.CATEGORY_KEY, pwaFullData.category)
                })
            }
        } else {
            binding.appCategory.visibility = View.GONE
        }

        // Handle clicks on description
        binding.appDescriptionContainer.setOnClickListener {
            val intent = Intent(this, ApplicationDescriptionActivity::class.java)
            intent.putExtra(APPLICATION_DESCRIPTION_KEY, application.pwaFullData!!.description)
            startActivity(intent)
        }

        // Load the app screenshots
        pwasBasicData.loadImagesAsyncly {
            showImages(it)
        }


        // Handle clicks on app permissions
        binding.exodusInfoContainer.visibility = View.GONE

        //app_information details
        binding.appInformationTitle.visibility = View.GONE
        binding.appVersionLayout.visibility = View.GONE
        binding.appUpdatedOnLayout.visibility = View.GONE
        binding.appRequires.visibility = View.GONE
        binding.appLicenceLayout.visibility = View.GONE
        binding.appPackageNameLayout.visibility =View.GONE


        application.addListener(this)
        stateChanged(application.state)

        // Handle clicks on app install button
        binding.installButtonLayout.appInstall.setOnClickListener {
            onPwaInstallButtonClick(pwaFullData)
        }
    }

    override fun onIconLoaded(application: Application, bitmap: Bitmap) {
        if (application == this.application) {
            binding.appIcon.setImageBitmap(bitmap)
        }
    }

    private fun getFormattedTimestamp(timestamp: String): String {
        return if (timestamp.contains(" ")) {
            timestamp.substring(0, timestamp.indexOf(" "))
        } else {
            timestamp
        }
    }

    private fun getFormattedMinSdkVersion(minSdkVersion: String): String {
        return if (minSdkVersion.contains(" (")) {
            minSdkVersion.substring(0, minSdkVersion.indexOf(" ("))
        } else {
            minSdkVersion
        }
    }

    private fun onInstallButtonClick(fullData: FullData) {
        // Make sure the APK is available for download


        if (fullData.getLastVersion() == null) {
            Snackbar.make(binding.container,
                          getString(Error.APK_UNAVAILABLE.description),
                          Snackbar.LENGTH_LONG).show()
            return
        }

        application.buttonClicked(this, this)
    }

    fun onPwaInstallButtonClick(fullData: PwaFullData) {

        val intent = Intent(this, PwaInstaller::class.java)
        intent.putExtra("NAME", fullData.name)
        intent.putExtra("URL", fullData.url)
        this.startActivity(intent)
    }

    override fun downloading(downloader: Downloader) {
        downloader.addListener(this)
    }

    @SuppressLint("SetTextI18n")
    override fun notifyDownloadProgress(count: Int, total: Int) {
        binding.appDownloadMb.text = "${toMiB(count)}/${toMiB(total)} MiB"
        binding.appDownloadPercentage.text =
            ((toMiB(count) / toMiB(total)) * 100).toInt().toString() + "%"
        binding.appDownloadProgress.max = total
        binding.appDownloadProgress.progress = count
    }

    override fun anErrorHasOccurred(error: Error) {
        Snackbar.make(binding.container,
                      getString(error.description),
                      Snackbar.LENGTH_LONG).show()
    }

    override fun stateChanged(state: State) {
        Execute({}, {
            binding.installButtonLayout.appInstall.text = resources.getString(state.installButtonTextId)
            when (state) {
                State.INSTALLED -> {
                    binding.installButtonLayout.appInstall.isEnabled =
                        Common.appHasLaunchActivity(this, application.packageName)
                    binding.appSize.visibility = View.VISIBLE
                    binding.appDownloadContainer.visibility = View.GONE
                }
                State.DOWNLOADING -> {
                    binding.installButtonLayout.appInstall.isEnabled = true
                    binding.appSize.visibility = View.GONE
                    binding.appDownloadMb.text = getString(R.string.state_installing)
                    binding.appDownloadPercentage.text = ""
                    binding.appDownloadProgress.progress = 0
                    binding.appDownloadContainer.visibility = View.VISIBLE
                }
                State.INSTALLING -> {
                    binding.installButtonLayout.appInstall.isEnabled = false
                    binding.appSize.visibility = View.VISIBLE
                    binding.appDownloadContainer.visibility = View.GONE
                }
                else -> {
                    binding.installButtonLayout.appInstall.isEnabled = true
                    binding.appSize.visibility = View.VISIBLE
                    binding.appDownloadContainer.visibility = View.GONE
                }
            }
        })
    }

    private fun setRatingBorder(rating: Float?) {
        when {
            rating!! >= 3.5f -> {
                binding.appRating.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_cat_green_ellipse, 0)
            }
            rating >= 2.0f -> {
                binding.appRating.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_yellow_ellipse, 0)
            }
            else -> {
                binding.appRating.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_red_ellipse, 0)
            }
        }
    }

    private fun setPrivacyRatingBorder(rating: Int) {
        when {
            rating >= 7 -> {
                binding.appPrivacyScore.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_cat_green_ellipse, 0)
            }
            rating >= 4 -> {
                binding.appPrivacyScore.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_yellow_ellipse, 0)
            }
            else -> {
                binding.appPrivacyScore.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_red_ellipse, 0)
            }
        }
    }

    private fun showImages(images: List<Bitmap>) {
        binding.appScreenshotsProgressBar.visibility = View.GONE
        if (images.isEmpty()) {
            binding.appScreenshotsError.visibility = View.VISIBLE
            return
        } else {
            binding.appScreenshotsError.visibility = View.GONE
        }
        binding.appImagesContainer.removeAllViews()
        images.forEach {
            val imageView = ImageView(this)
            val layoutParams =
                if (it.height < it.width) {
                    LinearLayout.LayoutParams((imageHeight * 1.78).toInt(), imageHeight)
                } else {
                    LinearLayout.LayoutParams(imageWidth, imageHeight)
                }
            layoutParams.leftMargin = imageMargin
            layoutParams.rightMargin = imageMargin
            imageView.layoutParams = layoutParams
            imageView.isClickable = true
            imageView.setImageBitmap(it)
            val outValue = TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            imageView.foreground = getDrawable(outValue.resourceId)
            binding.appImagesContainer.addView(imageView)
            imageView.setOnClickListener { _ ->
                val intent = Intent(this, ScreenshotsActivity::class.java)
                intent.putExtra(APPLICATION_PACKAGE_NAME_KEY, application.packageName)
                intent.putExtra(SELECTED_APPLICATION_SCREENSHOT_KEY, images.indexOf(it))
                startActivity(intent)
            }
            binding.appImagesScrollView.visibility = View.VISIBLE
            binding.appImagesContainer.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                application.buttonClicked(this, this)
            } else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Snackbar.make(binding.container, R.string.error_storage_permission_denied,
                              Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::application.isInitialized) {
            application.checkForStateUpdate(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::application.isInitialized) {
            application.removeListener(this)
            application.decrementUses()
            applicationManagerServiceConnection.unbindService(this)
        }
    }
}
