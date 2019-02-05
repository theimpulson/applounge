package foundation.e.apps.categories.category

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
import android.widget.RelativeLayout
import android.widget.TextView
import foundation.e.apps.R
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.applicationmanager.ApplicationManagerServiceConnection
import foundation.e.apps.applicationmanager.ApplicationManagerServiceConnectionCallback
import foundation.e.apps.categories.category.viewmodel.CategoryViewModel
import foundation.e.apps.categories.model.Category
import foundation.e.apps.common.ApplicationListAdapter
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Constants.CATEGORY_KEY
import kotlinx.android.synthetic.main.activity_category.*

class CategoryActivity : AppCompatActivity(), ApplicationManagerServiceConnectionCallback {

    private lateinit var category: Category
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private val applicationManagerServiceConnection =
            ApplicationManagerServiceConnection(this)
    private var applicationList = ArrayList<Application>()
    private var isLoadingMoreApplications = false

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
        val loadMoreContainer = findViewById<RelativeLayout>(R.id.load_more_container)
        progressBar = findViewById(R.id.progress_bar)
        val errorContainer = findViewById<LinearLayout>(R.id.error_container)
        val errorDescription = findViewById<TextView>(R.id.error_description)

        // Initialise UI elements
        recyclerView.visibility = View.GONE
        loadMoreContainer.visibility = View.GONE
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    loadMoreContainer.visibility = View.VISIBLE
                    recyclerView.scrollToPosition(applicationList.size - 1)
                    if (!isLoadingMoreApplications) {
                        isLoadingMoreApplications = true
                        categoryViewModel.loadApplications(this@CategoryActivity)
                    }
                } else {
                    loadMoreContainer.visibility = View.GONE
                }
            }
        })
        progressBar.visibility = View.VISIBLE
        errorContainer.visibility = View.GONE
        findViewById<TextView>(R.id.error_resolve).setOnClickListener {
            loadMoreContainer.visibility = View.GONE
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
                loadMoreContainer.visibility = View.GONE
                isLoadingMoreApplications = false
            }
        })

        // Bind to the screen error
        categoryViewModel.getScreenError().observe(this, Observer {
            if (it != null) {
                if (!isLoadingMoreApplications &&
                        applicationList.size >= Constants.RESULTS_PER_PAGE) {
                    errorDescription.text = getString(it.description)
                    errorContainer.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    loadMoreContainer.visibility = View.GONE
                } else {
                    loadMoreContainer.visibility = View.GONE
                    isLoadingMoreApplications = false
                }
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