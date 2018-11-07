package io.eelo.appinstaller.application

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.model.*
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Constants
import io.eelo.appinstaller.utils.Constants.APPLICATION_DESCRIPTION_KEY
import io.eelo.appinstaller.utils.Constants.APPLICATION_PACKAGE_NAME_KEY
import kotlinx.android.synthetic.main.activity_application.*
import kotlin.math.roundToInt

class ApplicationActivity : AppCompatActivity(), ApplicationStateListener {
    private lateinit var application: Application
    private lateinit var serviceConnection: ServiceConnection

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
        app_icon.setImageBitmap(application.data.iconImage?.getBitmap())
        app_title.text = application.data.name
        app_author.text = application.data.author
        app_category.text = Common.getCategoryTitle(application.data.category)
        app_description.text = Html.fromHtml(application.data.description)
        app_description_container.setOnClickListener {
            val intent = Intent(this, ApplicationDescriptionActivity::class.java)
            intent.putExtra(APPLICATION_DESCRIPTION_KEY, application.data.description)
            startActivity(intent)
        }
        app_rating.text = application.data.stars.toString() + "/10"
        app_privacy_score.text = application.data.privacyScore.toString() + "/10"
        app_energy_score.text = application.data.energyScore.toString() + "/10"
        app_install.setOnClickListener {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.STORAGE_PERMISSION_REQUEST_CODE)
                } else {
                    application.buttonClicked(this)
                }
            } else {
                application.buttonClicked(this)
            }
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
        var installButtonText = R.string.action_install
        var isInstallButtonEnabled = true
        when (state) {
            State.DOWNLOADING -> {
                installButtonText = R.string.state_downloading
                isInstallButtonEnabled = false
            }
            State.INSTALLING -> {
                installButtonText = R.string.state_installing
                isInstallButtonEnabled = false
            }
            State.INSTALLED -> {
                installButtonText = R.string.action_launch
            }
            State.NOT_UPDATED -> {
                installButtonText = R.string.action_update
            }
        }
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                return null
            }

            override fun onPostExecute(result: Void?) {
                app_install.text = resources.getString(installButtonText)
                app_install.isEnabled = isInstallButtonEnabled
            }
        }.executeOnExecutor(Common.EXECUTOR)
    }

    @SuppressLint("SetTextI18n")
    override fun downloading(downloader: Downloader) {
        downloader.addListener { count, total ->
            app_install.text = "${toMiB(count)}/${toMiB(total)} MiB"
        }
    }

    private fun toMiB(length: Int): Double {
        val inMiB = length.div(1048576)
        return inMiB.times(100.0).roundToInt().div(100.0)
    }

    override fun anErrorHasOccurred() {
        // TODO alert the user of the error (while downloading)
    }

    private fun createInstallManager(): InstallManager {
        startService(Intent(this, InstallManagerService::class.java))
        val blocker = Object()
        var installManager: InstallManager? = null
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                Messenger(service).send(Message.obtain(null, 0, { result: InstallManager ->
                    installManager = result
                    synchronized(blocker) {
                        blocker.notify()
                    }
                }))
            }

            override fun onServiceDisconnected(name: ComponentName) {}
        }
        bindService(Intent(this, InstallManagerService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        synchronized(blocker) {
            blocker.wait()
        }
        return installManager!!
    }

    inner class InitialiseTask : AsyncTask<String, Void, Void>() {
        override fun doInBackground(vararg params: String): Void? {
            val installManager = createInstallManager()
            application = installManager.findOrCreateApp(this@ApplicationActivity, ApplicationData(params[0]))
            if (application.data.fullnessLevel != 2) {
                application.searchFullData()
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            onApplicationInfoLoaded()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}
