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
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.common.ApplicationListAdapter
import io.eelo.appinstaller.updates.viewModel.UpdatesViewModel
import io.eelo.appinstaller.utils.Common

class UpdatesFragment : Fragment() {
    private lateinit var updatesViewModel: UpdatesViewModel
    private var installManager: InstallManager? = null

    fun initialise(installManager: InstallManager) {
        this.installManager = installManager
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (installManager == null) {
            return null
        }

        val view = inflater.inflate(R.layout.fragment_updates, container, false)

        updatesViewModel = ViewModelProviders.of(activity!!).get(UpdatesViewModel::class.java)
        val recyclerView = view.findViewById<RecyclerView>(R.id.app_list)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val errorContainer = view.findViewById<LinearLayout>(R.id.error_container)
        val errorDescription = view.findViewById<TextView>(R.id.error_description)

        // Initialise UI elements
        updatesViewModel.initialise(installManager!!)
        initializeRecyclerView(recyclerView)
        progressBar.visibility = View.VISIBLE
        errorContainer.visibility = View.GONE
        view.findViewById<TextView>(R.id.error_resolve).setOnClickListener {
            progressBar.visibility = View.VISIBLE
            updatesViewModel.loadApplicationList(context!!)
        }

        // Bind recycler view adapter to search results list in view model
        updatesViewModel.getApplications().observe(this, Observer {
            if (it!!.isNotEmpty()) {
                recyclerView.adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }
        })

        // Bind to the screen error
        updatesViewModel.getScreenError().observe(this, Observer {
            if (it != null) {
                errorDescription.text = activity!!.getString(Common.getScreenErrorDescriptionId(it))
                errorContainer.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            } else {
                errorContainer.visibility = View.GONE
            }
        })

        updatesViewModel.loadApplicationList(context!!)

        return view
    }

    private fun initializeRecyclerView(recyclerView: RecyclerView) {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ApplicationListAdapter(activity!!, updatesViewModel.getApplications().value!!)
    }
}