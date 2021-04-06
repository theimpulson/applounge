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

package foundation.e.apps.categories.category

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
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
import kotlinx.android.synthetic.main.error_layout.*

class CategoryActivity : AppCompatActivity(), ApplicationManagerServiceConnectionCallback {

    private lateinit var category: Category
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private val applicationManagerServiceConnection =
            ApplicationManagerServiceConnection(this)
    private var applicationList = ArrayList<Application>()
    private var isLoadingMoreApplications = false
    var accentColorOS=0;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
       getAccentColor()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val extras = intent.extras
        category = intent.getSerializableExtra(CATEGORY_KEY) as Category
        supportActionBar?.title = category.getTitle()

        categoryViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)
        recyclerView = findViewById(R.id.app_list)
        val loadMoreContainer = findViewById<RelativeLayout>(R.id.load_more_container)
        progressBar = findViewById(R.id.progress_bar)
        val errorContainer = findViewById<LinearLayout>(R.id.error_container)
        val errorDescription = findViewById<TextView>(R.id.error_description)

        //set accent color to Error button (Retry )
        findViewById<TextView>(R.id.error_resolve).setTextColor(Color.parseColor("#ffffff"))
        findViewById<TextView>(R.id.error_resolve).setBackgroundColor(accentColorOS)


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
        recyclerView.adapter = ApplicationListAdapter(this, applicationList, accentColorOS)

        // Bind to the list of applications in this activity's category
        categoryViewModel.getApplications().observe(this, Observer {
            if (it != null) {
                applicationList.clear()
                applicationList.addAll(it)
                progressBar.visibility = View.GONE
                recyclerView.adapter?.notifyDataSetChanged()
                recyclerView.visibility = View.VISIBLE
                loadMoreContainer.visibility = View.GONE
                isLoadingMoreApplications = false
            }
        })

        // Bind to the screen error
        categoryViewModel.getScreenError().observe(this, Observer {
            if (it != null) {
                if (!isLoadingMoreApplications) {
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

    /*
   * get Accent color from OS
   *
   *  */
    private fun getAccentColor() {

        accentColorOS=this.resources.getColor(R.color.colorAccent);
    }
}
