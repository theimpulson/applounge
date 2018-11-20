package io.eelo.appinstaller.application

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
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

    @SuppressLint("SetTextI18n")
    private fun onApplicationInfoLoaded() {
        val basicData = application.basicData!!
        val fullData = application.fullData!!

        application.loadIcon(app_icon)
        app_title.text = basicData.name
        app_author.text = basicData.author
        app_category.text = Common.getCategoryTitle(fullData.category)
        app_description.text = Html.fromHtml(fullData.description)
        app_description_container.setOnClickListener {
            val intent = Intent(this, ApplicationDescriptionActivity::class.java)
            intent.putExtra(APPLICATION_DESCRIPTION_KEY, application.fullData!!.description)
            startActivity(intent)
        }
        app_rating.text = basicData.score.toString() + "/10"
        app_privacy_score.text = fullData.privacyScore.toString() + "/10"
        app_energy_score.text = fullData.energyScore.toString() + "/10"
        app_install.setOnClickListener {
            application.buttonClicked(this)
        }
        application.addListener(this)
        stateChanged(application.state)
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

    override fun stateChanged(state: State) {
        Execute({}, {
            app_install.text = resources.getString(state.installButtonTextId)
            app_install.isEnabled = state.isInstallButtonEnabled
        })
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

    private fun initialise(packageName: String) {
        Execute({
            val installManager = installManagerGetter.connectAndGet(this)
            application = installManager.findOrCreateApp(packageName)
            application.assertFullData(this)
        }, {
            onApplicationInfoLoaded()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        installManagerGetter.disconnect(this)
    }
}
