package io.eelo.appinstaller.application

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Html
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
import io.eelo.appinstaller.utils.Execute
import kotlinx.android.synthetic.main.activity_application.*

class ApplicationActivity : AppCompatActivity(), ApplicationStateListener {
    private lateinit var application: Application
    private val installManagerGetter = InstallManagerGetter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Get application package name from intent
        val applicationPackageName: String? = intent.getStringExtra(APPLICATION_PACKAGE_NAME_KEY)
        if (!applicationPackageName.isNullOrEmpty()) {
            // Bind to the InstallManagerService and initialise applicationF
            InitialiseTask().executeOnExecutor(Common.EXECUTOR, applicationPackageName)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_application_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when {
            item?.itemId == R.id.action_share -> {
                // TODO Show sharing menu
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

    private fun onApplicationInfoLoaded() {
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

        if (application.data.name.isNotEmpty()) {
            appTitle.text = application.data.name
            appTitle.visibility = View.VISIBLE
        }

        if (application.data.author.isNotEmpty()) {
            appAuthor.text = application.data.author
            appAuthor.visibility = View.VISIBLE
        }

        if (application.data.category.isNotEmpty()) {
            appCategory.text = Common.getCategoryTitle(application.data.category)
            appCategory.visibility = View.VISIBLE
        }

        // TODO Show app size

        appInstall.setOnClickListener {
            application.buttonClicked(this, this)
        }

        if (application.data.description.isNotEmpty()) {
            appDescription.text = Html.fromHtml(application.data.description)
            appDescriptionContainer.visibility = View.VISIBLE
        }

        appDescriptionContainer.setOnClickListener {
            val intent = Intent(this, ApplicationDescriptionActivity::class.java)
            intent.putExtra(APPLICATION_DESCRIPTION_KEY, application.data.description)
            startActivity(intent)
        }

        appRating.text = application.data.stars.toString()
        appPrivacyScore.text = application.data.privacyScore.toString()
        appEnergyScore.text = application.data.energyScore.toString()

        if (application.data.images.isNotEmpty()) {
            // TODO Load app images/screenshots
            appImagesContainer.visibility = View.VISIBLE
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
                application.buttonClicked(this, this)
            } else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, resources.getString(R.string.error_storage_permission_denied), Toast.LENGTH_LONG).show()
            }
        }
    }

    private inner class InitialiseTask : AsyncTask<String, Any, Any>() {
        override fun doInBackground(vararg params: String): Any? {
            val context = this@ApplicationActivity
            val installManager = installManagerGetter.connectAndGet(context)
            application = installManager.findOrCreateApp(context, ApplicationData(params[0]))
            if (application.data.fullnessLevel != 2) {
                application.searchFullData(context)
            }
            return null
        }

        override fun onPostExecute(result: Any?) {
            onApplicationInfoLoaded()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        installManagerGetter.disconnect(this)
    }
}
