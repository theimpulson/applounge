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

package foundation.e.apps.updates

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import foundation.e.apps.R
import foundation.e.apps.application.model.Application
import foundation.e.apps.application.model.State
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.common.ApplicationListAdapter
import foundation.e.apps.updates.viewmodel.UpdatesViewModel


class UpdatesFragment() : Fragment() {
    private lateinit var updatesViewModel: UpdatesViewModel
    private var applicationManager: ApplicationManager? = null
    private lateinit var recyclerView: RecyclerView
    private var applicationList = ArrayList<Application>()
    var accentColorOS=0;
    lateinit var progressBar2:ProgressBar

    fun initialise(applicationManager: ApplicationManager, accentColorOS: Int) {
        this.applicationManager = applicationManager
        this.accentColorOS=accentColorOS;
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (applicationManager == null) {
            return null
        }

        val view = inflater.inflate(R.layout.fragment_updates, container, false)

        updatesViewModel = ViewModelProvider(this).get(UpdatesViewModel::class.java)
        recyclerView = view.findViewById(R.id.app_list)
        progressBar2 = view.findViewById<ProgressBar>(R.id.progress_bar2)
        val updateAll = view.findViewById<Button>(R.id.update_all)
        updateAll.setTextColor(accentColorOS)
        val splashContainer = view.findViewById<LinearLayout>(R.id.splash_container)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        progressBar.indeterminateDrawable.colorFilter = PorterDuffColorFilter(accentColorOS, PorterDuff.Mode.MULTIPLY)
        val reloadProgressBar = view.findViewById<ProgressBar>(R.id.progress_bar2)
        //progressBar.setProgressTintList(ColorStateList.valueOf(accentColorOS));
        reloadProgressBar.indeterminateDrawable.colorFilter = PorterDuffColorFilter(accentColorOS, PorterDuff.Mode.MULTIPLY)

        val errorContainer = view.findViewById<LinearLayout>(R.id.error_container)
        val errorDescription = view.findViewById<TextView>(R.id.error_description)

        //set accent color to Error button (Retry )
        view.findViewById<TextView>(R.id.error_resolve).setTextColor(Color.parseColor("#ffffff"))
        view.findViewById<TextView>(R.id.error_resolve).setBackgroundColor(accentColorOS)


        // Initialise UI elements
        updatesViewModel.initialise(applicationManager!!)
        recyclerView.visibility = View.GONE
        updateAll.isEnabled = false
        updateAll.setOnClickListener {
            applicationList.forEach { application ->
                if (application.state == State.NOT_UPDATED) {
                    application.buttonClicked(requireContext(), requireActivity())
                }
            }
        }
        progressBar.visibility = View.VISIBLE
        reloadProgressBar.visibility = View.GONE
        errorContainer.visibility = View.GONE
        splashContainer.visibility = View.GONE
        view.findViewById<TextView>(R.id.error_resolve).setOnClickListener {
            updateAll.isEnabled = false
            progressBar.visibility = View.VISIBLE
            updatesViewModel.loadApplicationList(requireContext())
        }

        // Initialise recycler view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ApplicationListAdapter(requireActivity(), applicationList, accentColorOS)

        // Bind recycler view adapter to outdated applications list in view model
        updatesViewModel.getApplications().observe(viewLifecycleOwner, Observer {
            if (it != null) {


                applicationList.clear()
                applicationList.addAll(it)
                progressBar.visibility = View.GONE
                reloadProgressBar.visibility=View.GONE
                recyclerView.adapter?.notifyDataSetChanged()
                recyclerView.scrollToPosition(0)
                if (applicationList.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    splashContainer.visibility = View.VISIBLE
                    updateAll.isEnabled = false
                } else {
                    splashContainer.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    updateAll.isEnabled = true
                }
            }
        })

        // Bind to the screen error
        updatesViewModel.getScreenError().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                errorDescription.text = requireActivity().getString(it.description)
                errorContainer.visibility = View.VISIBLE
                updateAll.isEnabled = false
                progressBar.visibility = View.GONE
                reloadProgressBar.visibility = View.GONE

                splashContainer.visibility = View.GONE
                recyclerView.visibility = View.GONE
            } else {
                errorContainer.visibility = View.GONE
            }
        })
        updatesViewModel.loadApplicationList(requireContext())


        return view
    }

    override fun onResume() {
        super.onResume()
        if (::updatesViewModel.isInitialized) {
            updatesViewModel.getApplications().value?.let {
                it.forEach { application ->
                    progressBar2.visibility=View.VISIBLE
                    application.checkForStateUpdate(requireContext())
                }
                val handler = Handler()
                handler.postDelayed({
                    progressBar2.visibility=View.GONE
                }, 10000)

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