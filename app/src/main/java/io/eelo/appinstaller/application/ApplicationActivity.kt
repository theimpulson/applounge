package io.eelo.appinstaller.application

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Html
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.model.*
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Common.toMiB
import io.eelo.appinstaller.utils.Constants
import io.eelo.appinstaller.utils.Constants.APPLICATION_DESCRIPTION_KEY
import io.eelo.appinstaller.utils.Constants.APPLICATION_PACKAGE_NAME_KEY
import io.eelo.appinstaller.utils.Constants.WEB_STORE_URL
import io.eelo.appinstaller.utils.Execute
import kotlinx.android.synthetic.main.activity_application.*
import java.text.DecimalFormat
import kotlin.math.roundToInt

class ApplicationActivity : AppCompatActivity(), ApplicationStateListener {
    private lateinit var application: Application
    private val installManagerGetter = InstallManagerGetter()
    private var imageWidth = 0
    private var imageHeight = 0
    private var imageMargin = 0

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
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, WEB_STORE_URL + application.basicData!!.id)
                    type = "text/plain"
                }
                startActivity(shareIntent)
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
    }

    private fun onApplicationInfoLoaded() {
        val basicData = application.basicData!!
        val fullData = application.fullData!!

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

        appTitle.visibility = View.GONE
        appAuthor.visibility = View.GONE
        appCategory.visibility = View.GONE
        appSize.visibility = View.GONE
        appDescriptionContainer.visibility = View.GONE
        appImagesContainer.visibility = View.GONE

        application.loadIcon(appIcon)

        if (basicData.name.isNotEmpty()) {
            appTitle.text = basicData.name
            appTitle.visibility = View.VISIBLE
        }

        if (basicData.author.isNotEmpty()) {
            appAuthor.text = basicData.author
            appAuthor.visibility = View.VISIBLE
        }

        if (fullData.category.isNotEmpty()) {
            appCategory.text = Common.getCategoryTitle(fullData.category)
            appCategory.visibility = View.VISIBLE
        }

        // TODO Show app size

        appInstall.setOnClickListener {
            application.buttonClicked(this)
        }

        if (fullData.description.isNotEmpty()) {
            appDescription.text = Html.fromHtml(fullData.description)
            appDescriptionContainer.visibility = View.VISIBLE
        }

        appDescriptionContainer.setOnClickListener {
            val intent = Intent(this, ApplicationDescriptionActivity::class.java)
            intent.putExtra(APPLICATION_DESCRIPTION_KEY, application.fullData!!.description)
            startActivity(intent)
        }

        val decimalFormat = DecimalFormat("##.0")
        appRating.text = decimalFormat.format(basicData.score).toString()
        appPrivacyScore.text = fullData.privacyScore.toString()
        appEnergyScore.text = fullData.energyScore.toString()

        basicData.loadImagesAsyncly {
            showImages(it)
        }

        application.addListener(this)
        stateChanged(application.state)
    }

    @SuppressLint("SetTextI18n")
    override fun downloading(downloader: Downloader) {
        downloader.addListener { count, total ->
            app_install.text = "${toMiB(count)}/${toMiB(total)} MiB"
        }
    }

    override fun anErrorHasOccurred() {
        // TODO alert the user of the error (while downloading)
    }

    override fun stateChanged(state: State) {
        Execute({}, {
            app_install.text = resources.getString(state.installButtonTextId)
            app_install.isEnabled = state.isInstallButtonEnabled
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Constants.STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                application.buttonClicked(this)
            } else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, resources.getString(R.string.error_storage_permission_denied), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initialise(packageName: String) {
        Execute({
            val installManager = installManagerGetter.connectAndGet(this)
            application = installManager.findOrCreateApp(packageName)
            application.assertFullData(this)
        }, {
            onApplicationInfoLoaded()
        })
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
            imageView.setImageBitmap(it)
            imagesContainer.addView(imageView)
        }
        imagesContainer.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        application.decrementUses()
        installManagerGetter.disconnect(this)
    }
}
