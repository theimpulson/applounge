package io.eelo.appinstaller.updates

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.State
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.common.ApplicationListAdapter
import io.eelo.appinstaller.updates.viewModel.UpdatesViewModel
import io.eelo.appinstaller.utils.Common

class UpdatesFragment : Fragment() {
    private lateinit var updatesViewModel: UpdatesViewModel
    private var applicationManager: ApplicationManager? = null
    private lateinit var recyclerView: RecyclerView
    private var applicationList = ArrayList<Application>()

    fun initialise(applicationManager: ApplicationManager) {
        this.applicationManager = applicationManager
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (applicationManager == null) {
            return null
        }

        val view = inflater.inflate(R.layout.fragment_updates, container, false)

        updatesViewModel = ViewModelProviders.of(activity!!).get(UpdatesViewModel::class.java)
        recyclerView = view.findViewById(R.id.app_list)
        val updateAll = view.findViewById<Button>(R.id.update_all)
        val splashContainer = view.findViewById<LinearLayout>(R.id.splash_container)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val errorContainer = view.findViewById<LinearLayout>(R.id.error_container)
        val errorDescription = view.findViewById<TextView>(R.id.error_description)

        // Initialise UI elements
        updatesViewModel.initialise(applicationManager!!)
        recyclerView.visibility = View.GONE
        updateAll.isEnabled = false
        updateAll.setOnClickListener {
            applicationList.forEach { application ->
                if (application.state == State.NOT_UPDATED) {
                    application.buttonClicked(activity!!)
                }
            }
        }
        progressBar.visibility = View.VISIBLE
        errorContainer.visibility = View.GONE
        splashContainer.visibility = View.GONE
        view.findViewById<TextView>(R.id.error_resolve).setOnClickListener {
            updateAll.isEnabled = false
            progressBar.visibility = View.VISIBLE
            updatesViewModel.loadApplicationList(context!!)
        }

        // Initialise recycler view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ApplicationListAdapter(activity!!, applicationList)

        // Bind recycler view adapter to outdated applications list in view model
        updatesViewModel.getApplications().observe(this, Observer {
            if (it != null) {
                applicationList.clear()
                applicationList.addAll(it)
                updateAll.isEnabled = true
                progressBar.visibility = View.GONE
                recyclerView.adapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(0)
                if (applicationList.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    splashContainer.visibility = View.VISIBLE
                } else {
                    splashContainer.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
        })

        // Bind to the screen error
        updatesViewModel.getScreenError().observe(this, Observer {
            if (it != null) {
                errorDescription.text = activity!!.getString(Common.getScreenErrorDescriptionId(it))
                errorContainer.visibility = View.VISIBLE
                updateAll.isEnabled = false
                progressBar.visibility = View.GONE
                splashContainer.visibility = View.GONE
                recyclerView.visibility = View.GONE
            } else {
                errorContainer.visibility = View.GONE
            }
        })

        if (updatesViewModel.getApplications().value == null ||
                updatesViewModel.getApplications().value!!.isEmpty()) {
            updatesViewModel.loadApplicationList(context!!)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (::updatesViewModel.isInitialized) {
            updatesViewModel.getApplications().value?.let {
                it.forEach { application ->
                    application.checkForStateUpdate(context!!)
                }
            }
        }
    }

    fun decrementApplicationUses() {
        if (::updatesViewModel.isInitialized) {
            updatesViewModel.getApplications().value?.let {
                it.forEach { application ->
                    application.decrementUses()
                }
            }
        }
    }
}