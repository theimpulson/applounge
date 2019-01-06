package io.eelo.appinstaller.application

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.CountDownTimer
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.ApplicationStateListener
import io.eelo.appinstaller.application.model.Downloader
import io.eelo.appinstaller.application.model.State
import io.eelo.appinstaller.application.model.data.FullData
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.applicationmanager.ApplicationManagerServiceConnection
import io.eelo.appinstaller.applicationmanager.ApplicationManagerServiceConnectionCallback
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Common.toMiB
import io.eelo.appinstaller.utils.Constants.APPLICATION_DESCRIPTION_KEY
import io.eelo.appinstaller.utils.Constants.APPLICATION_PACKAGE_NAME_KEY
import io.eelo.appinstaller.utils.Constants.SELECTED_APPLICATION_SCREENSHOT_KEY
import io.eelo.appinstaller.utils.Constants.WEB_STORE_URL
import io.eelo.appinstaller.utils.Error
import io.eelo.appinstaller.utils.Execute
import kotlinx.android.synthetic.main.activity_application.*
import kotlinx.android.synthetic.main.install_button_layout.*
import kotlin.math.roundToInt

class ApplicationActivity : AppCompatActivity(), ApplicationStateListener,
        ApplicationManagerServiceConnectionCallback {
    private lateinit var applicationPackageName: String
    private lateinit var application: Application
    private val applicationManagerServiceConnection =
            ApplicationManagerServiceConnection(this)
    private var imageWidth = 0
    private var imageHeight = 0
    private var imageMargin = 0
    private var defaultElevation = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (scroll_view.scrollY == 0) {
            toolbar.elevation = 0f
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            scroll_view.setOnScrollChangeListener { view, ia, ib, ic, id ->
                if (view.scrollY == 0) {
                    toolbar.elevation = 0f
                } else {
                    toolbar.elevation = defaultElevation
                }
            }
        }

        initialiseDimensions()

        val applicationPackageName: String? = intent.getStringExtra(APPLICATION_PACKAGE_NAME_KEY)
        if (!applicationPackageName.isNullOrEmpty()) {
            this.applicationPackageName = applicationPackageName!!
            applicationManagerServiceConnection.bindService(this)
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
                Snackbar.make(container,
                        getString(Common.getScreenErrorDescriptionId(error!!)),
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when {
            item?.itemId == R.id.action_share -> {
                if (::application.isInitialized) {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, WEB_STORE_URL + application.basicData!!.id)
                        type = "text/plain"
                    }
                    startActivity(shareIntent)
                }
            }
            item?.itemId == android.R.id.home -> {
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

    private fun onApplicationInfoLoaded() {
        val basicData = application.basicData!!
        val fullData = application.fullData!!

        // Load the app icon
        application.loadIcon(app_icon)

        // Set the app title
        if (basicData.name.isNotEmpty()) {
            app_title.text = basicData.name
        } else {
            app_title.text = getString(R.string.not_available_full)
        }

        // Set the app author
        if (basicData.author.isNotEmpty()) {
            app_author.text = basicData.author
        } else {
            app_author.text = getString(R.string.not_available_full)
        }

        // Set the app category
        if (fullData.category.getTitle().isNotEmpty()) {
            app_category.text = fullData.category.getTitle()
        } else {
            app_category.text = getString(R.string.not_available_full)
        }

        // Set the app description
        if (fullData.description.isNotEmpty()) {
            app_description.text = fullData.description
            app_description_container.isEnabled = true
        } else {
            app_description.text = getString(R.string.not_available_full)
            app_description_container.isEnabled = false
        }

        // Handle clicks on description
        app_description_container.setOnClickListener {
            val intent = Intent(this, ApplicationDescriptionActivity::class.java)
            intent.putExtra(APPLICATION_DESCRIPTION_KEY, application.fullData!!.description)
            startActivity(intent)
        }

        // Set the app rating
        if (basicData.ratings.rating != -1f) {
            app_rating.text = basicData.ratings.rating.toString() + "/5"
        } else {
            app_rating.text = getString(R.string.not_available)
        }
        setRatingBorder(basicData.ratings.rating)
        app_rating_container.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setIcon(R.drawable.ic_dialog_info)
            alertDialog.setTitle(R.string.app_rating)
            alertDialog.setMessage(getString(R.string.app_rating_description))
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok))
            { _, _ ->
                alertDialog.dismiss()
            }
            alertDialog.show()
        }

        // TODO Set the app energy score
        app_energy_container.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setIcon(R.drawable.ic_dialog_info)
            alertDialog.setTitle(R.string.app_energy_score)
            alertDialog.setMessage(getString(R.string.app_energy_description))
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok))
            { _, _ ->
                alertDialog.dismiss()
            }
            alertDialog.show()
        }

        // Load the app screenshots
        basicData.loadImagesAsyncly {
            showImages(it)
        }

        //Set the app licence
        if (fullData.licence.isNotEmpty()) {
            app_licence.text = fullData.licence
        } else {
            app_licence.text = getString(R.string.not_available)
        }

        if (fullData.getLastVersion() != null) {
            // Set app size
            if (fullData.getLastVersion()!!.fileSize.isNotEmpty()) {
                app_size.text = fullData.getLastVersion()!!.fileSize
            } else {
                app_size.text = getString(R.string.not_available)
            }

            // Set the app privacy rating
            if (fullData.getLastVersion()!!.privacyRating != null &&
                    fullData.getLastVersion()!!.privacyRating != -1) {
                app_privacy_score.text = fullData.getLastVersion()!!.privacyRating.toString() + "/10"
                setPrivacyRatingBorder(fullData.getLastVersion()!!.privacyRating!!)
            } else {
                app_privacy_score.text = getString(R.string.not_available)
                setPrivacyRatingBorder(-1)
            }
            app_privacy_container.setOnClickListener {
                val alertDialog = AlertDialog.Builder(this).create()
                alertDialog.setIcon(R.drawable.ic_dialog_info)
                alertDialog.setTitle(R.string.app_privacy_score)
                alertDialog.setMessage(getString(R.string.app_privacy_description))
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok))
                { _, _ ->
                    alertDialog.dismiss()
                }
                alertDialog.show()
            }

            // Set app version
            if (fullData.getLastVersion()!!.version.isNotEmpty()) {
                app_version.text = fullData.getLastVersion()!!.version
            } else {
                app_version.text = getString(R.string.not_available)
            }

            // Set app update timestamp
            if (fullData.getLastVersion()!!.createdOn.isNotEmpty()) {
                app_updated_on.text = fullData.getLastVersion()!!.createdOn
            } else {
                app_updated_on.text = getString(R.string.not_available)
            }

            // Set app minimum required Android version
            if (fullData.getLastVersion()!!.minAndroid.isNotEmpty()) {
                app_min_android.text = fullData.getLastVersion()!!.minAndroid
            } else {
                app_min_android.text = getString(R.string.not_available)
            }
        } else {
            // Set app size
            app_size.text = getString(R.string.not_available)

            // Set app privacy rating
            app_privacy_score.text = getString(R.string.not_available)
            setPrivacyRatingBorder(-1)
            app_privacy_container.setOnClickListener {
                val alertDialog = AlertDialog.Builder(this).create()
                alertDialog.setIcon(R.drawable.ic_dialog_info)
                alertDialog.setTitle(R.string.app_privacy_score)
                alertDialog.setMessage(getString(R.string.app_privacy_description))
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok))
                { _, _ ->
                    alertDialog.dismiss()
                }
                alertDialog.show()
            }

            // Set app version
            app_version.text = getString(R.string.not_available_full)

            // Set app update timestamp
            app_updated_on.text = getString(R.string.not_available_full)

            // Set app minimum required Android version
            app_min_android.text = getString(R.string.not_available_full)
        }

        // Handle clicks on app permissions
        app_permissions_container.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setTitle(R.string.app_permissions_title)
            if (fullData.getLastVersion() != null) {
                if (fullData.getLastVersion()!!.exodusPermissions.isNotEmpty()) {
                    var message = ""
                    var index = 0
                    fullData.getLastVersion()!!.exodusPermissions.forEach { permission ->
                        message += permission
                        if (index != fullData.getLastVersion()!!.exodusPermissions.size - 1) {
                            message += "\n"
                        }
                        index++
                    }
                    alertDialog.setMessage(message)
                } else {
                    alertDialog.setMessage(getString(R.string.not_available_full))
                }
            } else {
                alertDialog.setMessage(getString(R.string.not_available_full))
            }
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok))
            { _, _ ->
                alertDialog.dismiss()
            }
            alertDialog.show()
        }

        // Handle clicks on app trackers
        app_trackers_container.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setTitle(R.string.app_trackers_title)
            if (fullData.getLastVersion() != null) {
                if (fullData.getLastVersion()!!.exodusTrackers.isNotEmpty()) {
                    var message = ""
                    var index = 0
                    fullData.getLastVersion()!!.exodusTrackers.forEach { tracker ->
                        message += tracker
                        if (index != fullData.getLastVersion()!!.exodusTrackers.size - 1) {
                            message += "\n"
                        }
                        index++
                    }
                    alertDialog.setMessage(message)
                } else {
                    alertDialog.setMessage(getString(R.string.not_available_full))
                }
            } else {
                alertDialog.setMessage(getString(R.string.not_available_full))
            }
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok))
            { _, _ ->
                alertDialog.dismiss()
            }
            alertDialog.show()
        }

        application.addListener(this)
        stateChanged(application.state)

        // Handle clicks on app install button
        app_install.setOnClickListener {
            onInstallButtonClick(fullData)
        }
    }

    private fun onInstallButtonClick(fullData: FullData) {
        // Make sure the APK is available for download
        if (fullData.getLastVersion() == null) {
            Snackbar.make(container,
                    getString(Common.getScreenErrorDescriptionId(Error.APK_UNAVAILABLE)),
                    Snackbar.LENGTH_LONG).show()
            return
        }
        application.buttonClicked(this)
    }

    @SuppressLint("SetTextI18n")
    override fun downloading(downloader: Downloader) {
        downloader.addListener { count, total ->
            app_download_mb.text = "${toMiB(count)}/${toMiB(total)} MiB"
            app_download_percentage.text =
                    ((toMiB(count) / toMiB(total)) * 100).toInt().toString() + "%"
            app_download_progress.max = total
            app_download_progress.progress = count
        }
    }

    override fun anErrorHasOccurred() {
        // TODO alert the user of the error (while downloading)
    }

    override fun stateChanged(state: State) {
        Execute({}, {
            app_install.text = resources.getString(state.installButtonTextId)
            when (state) {
                State.DOWNLOADING -> {
                    app_install.setBackgroundResource(R.drawable.app_install_border_simple)
                    app_install.setTextColor(resources.getColor(android.R.color.primary_text_light))
                    app_install.isEnabled = true
                    app_size.visibility = View.GONE
                    app_download_container.visibility = View.VISIBLE
                }
                State.INSTALLING -> {
                    app_install.setBackgroundResource(R.drawable.app_install_border)
                    app_install.setTextColor(resources.getColor(android.R.color.primary_text_dark))
                    app_install.isEnabled = false
                    app_size.visibility = View.VISIBLE
                    app_download_container.visibility = View.GONE
                }
                else -> {
                    app_install.setBackgroundResource(R.drawable.app_install_border)
                    app_install.setTextColor(resources.getColor(android.R.color.primary_text_dark))
                    app_install.isEnabled = true
                    app_size.visibility = View.VISIBLE
                    app_download_container.visibility = View.GONE
                }
            }
        })
    }

    private fun setRatingBorder(rating: Float) {
        when {
            rating >= 7f -> {
                app_rating.setBackgroundResource(R.drawable.app_border_good)
            }
            rating >= 4f -> {
                app_rating.setBackgroundResource(R.drawable.app_border_neutral)
            }
            else -> {
                app_rating.setBackgroundResource(R.drawable.app_border_bad)
            }
        }
    }

    private fun setPrivacyRatingBorder(rating: Int) {
        when {
            rating >= 7 -> {
                app_privacy_score.setBackgroundResource(R.drawable.app_border_good)
            }
            rating >= 4 -> {
                app_privacy_score.setBackgroundResource(R.drawable.app_border_neutral)
            }
            else -> {
                app_privacy_score.setBackgroundResource(R.drawable.app_border_bad)
            }
        }
    }

    private fun showImages(images: List<Bitmap>) {
        app_screenshots_progress_bar.visibility = View.GONE
        if (images.isEmpty()) {
            app_screenshots_error.visibility = View.VISIBLE
            return
        } else {
            app_screenshots_error.visibility = View.GONE
        }
        app_images_container.removeAllViews()
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
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                imageView.foreground = getDrawable(outValue.resourceId)
            }
            app_images_container.addView(imageView)
            imageView.setOnClickListener { _ ->
                val intent = Intent(this, ScreenshotsActivity::class.java)
                intent.putExtra(APPLICATION_PACKAGE_NAME_KEY, application.packageName)
                intent.putExtra(SELECTED_APPLICATION_SCREENSHOT_KEY, images.indexOf(it))
                startActivity(intent)
            }
            app_images_scroll_view.visibility = View.VISIBLE
            app_images_container.visibility = View.VISIBLE
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
        application.decrementUses()
        applicationManagerServiceConnection.unbindService(this)
    }
}
