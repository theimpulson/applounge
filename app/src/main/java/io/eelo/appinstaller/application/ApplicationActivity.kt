package io.eelo.appinstaller.application

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
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
import io.eelo.appinstaller.ScreenshotsActivity
import io.eelo.appinstaller.application.model.*
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

class ApplicationActivity : AppCompatActivity(), ApplicationStateListener {
    private lateinit var application: Application
    private val installManagerGetter = InstallManagerGetter()
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

        initialiseDimensions()

        val applicationPackageName: String? = intent.getStringExtra(APPLICATION_PACKAGE_NAME_KEY)
        if (!applicationPackageName.isNullOrEmpty()) {
            initialise(applicationPackageName!!)
        }
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

        if (fullData.getLastVersion() == null) {
            Toast.makeText(this, getString(Common.getScreenErrorDescriptionId(Error.APK_UNAVAILABLE)), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val appIcon = findViewById<ImageView>(R.id.app_icon)
        val appTitle = findViewById<TextView>(R.id.app_title)
        val appAuthor = findViewById<TextView>(R.id.app_author)
        val appCategory = findViewById<TextView>(R.id.app_category)
        val appSize = findViewById<TextView>(R.id.app_size)
        val appInstall = findViewById<Button>(R.id.app_install)
        val appDescriptionContainer = findViewById<RelativeLayout>(R.id.app_description_container)
        val appDescription = findViewById<TextView>(R.id.app_description)
        val appRating = findViewById<TextView>(R.id.app_rating)
        val appPrivacyScore = findViewById<TextView>(R.id.app_privacy_score)
        val appEnergyScore = findViewById<TextView>(R.id.app_energy_score)
        val appImagesContainer = findViewById<LinearLayout>(R.id.app_images_container)
        val appImagesDivider = findViewById<View>(R.id.app_images_divider)
        val appVersion = findViewById<TextView>(R.id.app_version)
        val appUpdatedOn = findViewById<TextView>(R.id.app_updated_on)
        val appMinAndroid = findViewById<TextView>(R.id.app_min_android)
        val appLicence = findViewById<TextView>(R.id.app_licence)

        appTitle.visibility = View.GONE
        appAuthor.visibility = View.GONE
        appCategory.visibility = View.GONE
        app_download_container.visibility = View.GONE
        appSize.visibility = View.GONE
        appDescriptionContainer.visibility = View.GONE
        app_screenshots_container.visibility = View.GONE
        app_images_scroll_view.visibility = View.GONE
        appImagesContainer.visibility = View.GONE
        appImagesDivider.visibility = View.GONE

        application.loadIcon(appIcon)

        if (basicData.name.isNotEmpty()) {
            appTitle.text = basicData.name
            appTitle.visibility = View.VISIBLE
        }

        if (basicData.author.isNotEmpty()) {
            appAuthor.text = basicData.author
            appAuthor.visibility = View.VISIBLE
        }

        if (fullData.category.getTitle().isNotEmpty()) {
            appCategory.text = fullData.category.getTitle()
            appCategory.visibility = View.VISIBLE
        }

        if (fullData.getLastVersion()!!.fileSize.isNotEmpty()) {
            appSize.text = fullData.getLastVersion()!!.fileSize
            appSize.visibility = View.VISIBLE
        }

        appInstall.setOnClickListener {
            application.buttonClicked(this)
        }

        if (fullData.description.isNotEmpty()) {
            appDescription.text = fullData.description
            appDescriptionContainer.visibility = View.VISIBLE
        }

        appDescriptionContainer.setOnClickListener {
            val intent = Intent(this, ApplicationDescriptionActivity::class.java)
            intent.putExtra(APPLICATION_DESCRIPTION_KEY, application.fullData!!.description)
            startActivity(intent)
        }

        if (basicData.ratings.rating != -1f) {
            appRating.text = basicData.ratings.rating.toString() + "/5"
        } else {
            appRating.text = getString(R.string.not_available)
        }
        setRatingBorder(basicData.ratings.rating, appRating)
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
        if (fullData.getLastVersion()!!.privacyRating != null &&
                fullData.getLastVersion()!!.privacyRating != -1) {
            appPrivacyScore.text = fullData.getLastVersion()!!.privacyRating.toString() + "/10"
            setPrivacyRatingBorder(fullData.getLastVersion()!!.privacyRating!!, appPrivacyScore)
        } else {
            appPrivacyScore.text = getString(R.string.not_available)
            setPrivacyRatingBorder(-1, appPrivacyScore)
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
        /*if (basicData.score != -1f) {
            appEnergyScore.text = fullData.energyScore.toString()
        } else {*/
        appEnergyScore.text = getString(R.string.not_available)
        //}
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

        basicData.loadImagesAsyncly {
            showImages(it)
        }

        if (fullData.getLastVersion()!!.version.isNotEmpty()) {
            appVersion.text = fullData.getLastVersion()!!.version
        } else {
            appVersion.text = getString(R.string.not_available)
        }

        if (fullData.getLastVersion()!!.createdOn.isNotEmpty()) {
            appUpdatedOn.text = fullData.getLastVersion()!!.createdOn
        } else {
            appUpdatedOn.text = getString(R.string.not_available)
        }

        if (fullData.getLastVersion()!!.minAndroid.isNotEmpty()) {
            appMinAndroid.text = fullData.getLastVersion()!!.minAndroid
        } else {
            appMinAndroid.text = getString(R.string.not_available)
        }

        if (fullData.licence.isNotEmpty()) {
            appLicence.text = fullData.licence
        } else {
            appLicence.text = getString(R.string.not_available)
        }

        application.addListener(this)
        stateChanged(application.state)

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

        app_permissions_container.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setTitle(R.string.app_permissions_title)
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
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok))
            { _, _ ->
                alertDialog.dismiss()
            }
            alertDialog.show()
        }

        app_trackers_container.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setTitle(R.string.app_trackers_title)
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
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok))
            { _, _ ->
                alertDialog.dismiss()
            }
            alertDialog.show()
        }
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

    private fun initialise(packageName: String) {
        var error: Error? = null
        Execute({
            val installManager = installManagerGetter.connectAndGet(this)
            application = installManager.findOrCreateApp(packageName)
            error = application.assertFullData(this)
        }, {
            if (error == null) {
                onApplicationInfoLoaded()
            } else {
                Toast.makeText(this, getString(Common.getScreenErrorDescriptionId(error!!)), Toast.LENGTH_LONG).show()
                finish()
            }
        })
    }

    private fun setRatingBorder(rating: Float, textView: TextView) {
        when {
            rating >= 7f -> {
                textView.setBackgroundResource(R.drawable.app_border_good)
            }
            rating >= 4f -> {
                textView.setBackgroundResource(R.drawable.app_border_neutral)
            }
            else -> {
                textView.setBackgroundResource(R.drawable.app_border_bad)
            }
        }
    }

    private fun setPrivacyRatingBorder(rating: Int, textView: TextView) {
        when {
            rating >= 7 -> {
                textView.setBackgroundResource(R.drawable.app_border_good)
            }
            rating >= 4 -> {
                textView.setBackgroundResource(R.drawable.app_border_neutral)
            }
            else -> {
                textView.setBackgroundResource(R.drawable.app_border_bad)
            }
        }
    }

    private fun showImages(images: List<Bitmap>) {
        val imagesContainer = app_images_container
        imagesContainer.removeAllViews()
        images.forEach {
            val imageView = ImageView(this)
            val layoutParams =
                    if (it.height < it.width) {
                        LinearLayout.LayoutParams(imageHeight, imageWidth)
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
            imagesContainer.addView(imageView)
            imageView.setOnClickListener { _ ->
                val intent = Intent(this, ScreenshotsActivity::class.java)
                intent.putExtra(APPLICATION_PACKAGE_NAME_KEY, application.packageName)
                intent.putExtra(SELECTED_APPLICATION_SCREENSHOT_KEY, images.indexOf(it))
                startActivity(intent)
            }
            app_screenshots_container.visibility = View.VISIBLE
            app_images_scroll_view.visibility = View.VISIBLE
            imagesContainer.visibility = View.VISIBLE
            app_images_divider.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        application.decrementUses()
        installManagerGetter.disconnect(this)
    }
}
