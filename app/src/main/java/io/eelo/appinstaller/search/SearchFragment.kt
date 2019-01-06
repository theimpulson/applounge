package io.eelo.appinstaller.search

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.support.v4.app.Fragment
import android.support.v4.widget.CursorAdapter
import android.support.v4.widget.SimpleCursorAdapter
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.model.State
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.common.ApplicationListAdapter
import io.eelo.appinstaller.search.viewModel.SearchViewModel
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Constants.SUGGESTION_KEY

class SearchFragment : Fragment(), SearchView.OnQueryTextListener, SearchView.OnSuggestionListener {
    private lateinit var searchViewModel: SearchViewModel
    private var focusView: View? = null
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var splashContainer: LinearLayout
    private var applicationList = ArrayList<Application>()
    private var applicationManager: ApplicationManager? = null

    fun initialise(applicationManager: ApplicationManager) {
        this.applicationManager = applicationManager
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (applicationManager == null) {
            return null
        }

        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchViewModel = ViewModelProviders.of(activity!!).get(SearchViewModel::class.java)
        focusView = view.findViewById(R.id.view)
        searchView = view.findViewById(R.id.search_view)
        recyclerView = view.findViewById(R.id.app_list)
        progressBar = view.findViewById(R.id.progress_bar)
        splashContainer = view.findViewById(R.id.splash_container)
        val errorContainer = view.findViewById<LinearLayout>(R.id.error_container)
        val errorDescription = view.findViewById<TextView>(R.id.error_description)

        searchViewModel.initialise(applicationManager!!)
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE
        if (searchViewModel.getScreenError().value == null && searchViewModel.getApplications().value!!.isEmpty()) {
            splashContainer.visibility = View.VISIBLE
        } else {
            splashContainer.visibility = View.GONE
        }
        errorContainer.visibility = View.GONE
        view.findViewById<TextView>(R.id.error_resolve).setOnClickListener {
            progressBar.visibility = View.VISIBLE
            onQueryTextSubmit(searchView.query.toString())
        }

        // Initialise search view
        val from = arrayOf(SUGGESTION_KEY)
        val to = intArrayOf(android.R.id.text1)
        searchView.suggestionsAdapter = SimpleCursorAdapter(context,
                android.R.layout.simple_list_item_1, null, from, to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)
        populateSuggestionsAdapter(searchViewModel.getSuggestions().value!!)

        // Initialise recycler view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ApplicationListAdapter(activity!!, applicationList)

        // Bind search view suggestions adapter to search suggestions list in view model
        searchViewModel.getSuggestions().observe(this, Observer {
            populateSuggestionsAdapter(it!!)
        })

        // Bind recycler view adapter to search results list in view model
        searchViewModel.getApplications().observe(this, Observer {
            applicationList.clear()
            applicationList.addAll(searchViewModel.getApplications().value!!)
            progressBar.visibility = View.GONE
            recyclerView.adapter.notifyDataSetChanged()
            recyclerView.scrollToPosition(0)
            if (applicationList.isEmpty()) recyclerView.visibility = View.GONE else recyclerView.visibility = View.VISIBLE
        })

        // Bind to the screen error
        searchViewModel.getScreenError().observe(this, Observer {
            if (it != null) {
                errorDescription.text = activity!!.getString(Common.getScreenErrorDescriptionId(it))
                errorContainer.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            } else {
                errorContainer.visibility = View.GONE
            }
        })

        // Handle suggestion clicks
        searchView.setOnSuggestionListener(this)

        // Handle search queries
        searchView.setOnQueryTextListener(this)

        return view
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let {
            hideKeyboard(activity as Activity)
            focusView!!.requestFocus()
            recyclerView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            splashContainer.visibility = View.GONE
            searchViewModel.onSearchQuerySubmitted(context!!, it)
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        searchView.query?.let {
            searchViewModel.onSearchQueryChanged(context!!, it.toString())
        }
        return true
    }

    override fun onSuggestionSelect(position: Int): Boolean {
        return true
    }

    override fun onSuggestionClick(position: Int): Boolean {
        searchView.setQuery(searchViewModel.getSuggestions().value!![position], true)
        return true
    }

    private fun populateSuggestionsAdapter(suggestions: ArrayList<String>) {
        val cursor = MatrixCursor(arrayOf(BaseColumns._ID, SUGGESTION_KEY))
        for (i in 0 until suggestions.size) {
            cursor.addRow(arrayOf(i, suggestions[i]))
        }
        searchView.suggestionsAdapter.changeCursor(cursor)
    }

    private fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onResume() {
        focusView?.let {
            focusView!!.requestFocus()
        }
        if (::searchViewModel.isInitialized) {
            searchViewModel.getApplications().value!!.forEach { application ->
                if (application.state == State.INSTALLING ||
                        application.state == State.INSTALLED) {
                    application.checkForStateUpdate(context!!)
                }
            }
        }
        super.onResume()
    }

    fun decrementApplicationUses() {
        applicationList.forEach {
            it.decrementUses()
        }
    }
}
