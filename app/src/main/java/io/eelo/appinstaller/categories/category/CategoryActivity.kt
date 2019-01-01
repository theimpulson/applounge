package io.eelo.appinstaller.categories.category

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManagerServiceConnection
import io.eelo.appinstaller.application.model.State
import io.eelo.appinstaller.categories.category.viewmodel.CategoryViewModel
import io.eelo.appinstaller.categories.model.Category
import io.eelo.appinstaller.common.ApplicationListAdapter
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Constants.CATEGORY_KEY

class CategoryActivity : AppCompatActivity() {

    private lateinit var category: Category
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private var applicationList = ArrayList<Application>()
    private val applicationManagerServiceConnection = ApplicationManagerServiceConnection()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        category = intent.getSerializableExtra(CATEGORY_KEY) as Category
        supportActionBar?.title = category.getTitle()

        categoryViewModel = ViewModelProviders.of(this).get(CategoryViewModel::class.java)
        recyclerView = findViewById(R.id.app_list)
        progressBar = findViewById(R.id.progress_bar)
        val errorContainer = findViewById<LinearLayout>(R.id.error_container)
        val errorDescription = findViewById<TextView>(R.id.error_description)

        // Initialise UI elements
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        errorContainer.visibility = View.GONE
        findViewById<TextView>(R.id.error_resolve).setOnClickListener {
            progressBar.visibility = View.VISIBLE
            categoryViewModel.loadApplications(this)
        }

        // Initialise recycler view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ApplicationListAdapter(this, applicationList)

        // Bind to the list of applications in this activity's category
        categoryViewModel.getApplications().observe(this, Observer {
            if (it!!.isNotEmpty()) {
                applicationList.clear()
                applicationList.addAll(it)
                progressBar.visibility = View.GONE
                recyclerView.adapter.notifyDataSetChanged()
                recyclerView.visibility = View.VISIBLE
                recyclerView.scrollToPosition(0)
            }
        })

        // Bind to the screen error
        categoryViewModel.getScreenError().observe(this, Observer {
            if (it != null) {
                errorDescription.text = getString(Common.getScreenErrorDescriptionId(it))
                errorContainer.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            } else {
                errorContainer.visibility = View.GONE
            }
        })

        object : AsyncTask<Void, Void, Void>() {

            override fun doInBackground(vararg p0: Void?): Void? {
                val installManager = applicationManagerServiceConnection.connectAndGet(this@CategoryActivity)
                categoryViewModel.initialise(installManager, category.id)
                return null
            }

            override fun onPostExecute(result: Void?) {
                categoryViewModel.loadApplications(this@CategoryActivity)
            }
        }.executeOnExecutor(Common.EXECUTOR)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home ->
                finish()
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        if (::categoryViewModel.isInitialized) {
            categoryViewModel.getApplications().value!!.forEach { application ->
                if (application.state == State.INSTALLING ||
                        application.state == State.INSTALLED) {
                    application.checkForStateUpdate(this)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        applicationList.forEach {
            it.decrementUses()
        }
        applicationManagerServiceConnection.disconnect(this)
    }
}
