package io.eelo.appinstaller.categories.category

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
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
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.applicationmanager.ApplicationManagerServiceConnection
import io.eelo.appinstaller.applicationmanager.ApplicationManagerServiceConnectionCallback
import io.eelo.appinstaller.categories.category.viewmodel.CategoryViewModel
import io.eelo.appinstaller.categories.model.Category
import io.eelo.appinstaller.common.ApplicationListAdapter
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Constants
import io.eelo.appinstaller.utils.Constants.CATEGORY_KEY
import kotlinx.android.synthetic.main.activity_category.*

class CategoryActivity : AppCompatActivity(), ApplicationManagerServiceConnectionCallback {

    private lateinit var category: Category
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private val applicationManagerServiceConnection =
            ApplicationManagerServiceConnection(this)
    private var applicationList = ArrayList<Application>()

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
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    categoryViewModel.loadApplications(this@CategoryActivity)
                }
            }
        })
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
            if (it != null) {
                applicationList.clear()
                applicationList.addAll(it)
                progressBar.visibility = View.GONE
                recyclerView.adapter.notifyDataSetChanged()
                recyclerView.visibility = View.VISIBLE
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

        applicationManagerServiceConnection.bindService(this)
    }

    override fun onServiceBind(applicationManager: ApplicationManager) {
        categoryViewModel.initialise(applicationManager, category.id)
        if (categoryViewModel.getApplications().value == null) {
            categoryViewModel.loadApplications(this)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home ->
                finish()
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == Constants.STORAGE_PERMISSION_REQUEST_CODE &&
                grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Snackbar.make(container, R.string.error_storage_permission_denied,
                    Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::categoryViewModel.isInitialized) {
            categoryViewModel.getApplications().value?.let {
                it.forEach { application ->
                    application.checkForStateUpdate(this)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        categoryViewModel.getApplications().value?.let {
            it.forEach { application ->
                application.decrementUses()
            }
        }
        applicationManagerServiceConnection.unbindService(this)
    }
}
