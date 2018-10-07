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
import io.eelo.appinstaller.R
import io.eelo.appinstaller.common.ApplicationListAdapter

class UpdatesFragment : Fragment(), ApplicationListAdapter.AdapterClickListener {
    private lateinit var updatesViewModel: UpdatesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_updates, container, false)

        updatesViewModel = ViewModelProviders.of(this).get(UpdatesViewModel::class.java)
        val recyclerView = view.findViewById<RecyclerView>(R.id.app_list)
        val viewManager = LinearLayoutManager(context)
        val adapter = ApplicationListAdapter(context!!, updatesViewModel.getApplications().value!!)

        // Initialise recycler view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = viewManager
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(this)

        // Bind recycler view adapter to search results list in view model
        updatesViewModel.getApplications().observe(this, Observer {
            recyclerView.adapter.notifyDataSetChanged()
        })

        // Get a list of all apps with updates
        updatesViewModel.loadApplicationList()

        return view
    }

    override fun onItemClick(position: Int) {
        updatesViewModel.onApplicationClick(context!!, updatesViewModel.getApplications().value!![position])
    }
}