/*
    Copyright (C) 2019  e Foundation

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.search

import android.app.Activity
import android.database.MatrixCursor
import android.graphics.Color
import android.os.Bundle
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import foundation.e.apps.R
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.common.ApplicationListAdapter
import foundation.e.apps.search.viewmodel.SearchViewModel
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Constants.SUGGESTION_KEY

class SearchFragment : Fragment(), SearchView.OnQueryTextListener, SearchView.OnSuggestionListener {
    private lateinit var searchViewModel: SearchViewModel
    private var focusView: View? = null
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var splashContainer: LinearLayout
    private var applicationList = ArrayList<Application>()
    private var applicationManager: ApplicationManager? = null
    private var isLoadingMoreApplications = false
    var accentColorOS=0;

    fun initialise(applicationManager: ApplicationManager, accentColorOS: Int) {
        this.applicationManager = applicationManager
        this.accentColorOS=accentColorOS;
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)

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
        var error_resolve =view.findViewById<TextView>(R.id.error_resolve)
        val errorContainer = view.findViewById<LinearLayout>(R.id.error_container)
        val errorDescription = view.findViewById<TextView>(R.id.error_description)
        val loadMoreContainer = view.findViewById<RelativeLayout>(R.id.load_more_container)
//set accent color to Error button (Retry )
        error_resolve.setTextColor(Color.parseColor("#ffffff"))
        error_resolve.setBackgroundColor(accentColorOS)


        error_resolve.visibility=View.GONE
        searchViewModel.initialise(applicationManager!!)
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE
        if (searchViewModel.getScreenError().value == null &&
                searchViewModel.getApplications().value == null) {
            splashContainer.visibility = View.VISIBLE
        } else {
            splashContainer.visibility = View.GONE
        }
        errorContainer.visibility = View.GONE
        view.findViewById<TextView>(R.id.error_resolve).setOnClickListener {
            loadMoreContainer.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            onQueryTextSubmit(searchView.query.toString())
        }

        // Initialise search view
        val from = arrayOf(SUGGESTION_KEY)
        val to = intArrayOf(android.R.id.text1)
        searchView.suggestionsAdapter = SimpleCursorAdapter(context,
                android.R.layout.simple_list_item_1, null, from, to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)

        // Initialise recycler view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ApplicationListAdapter(activity!!, applicationList, 0)
        loadMoreContainer.visibility = View.GONE
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) &&
                        applicationList.size >= Constants.RESULTS_PER_PAGE) {
                    loadMoreContainer.visibility = View.VISIBLE
                    recyclerView.scrollToPosition(applicationList.size - 1)
                    if (!isLoadingMoreApplications) {
                        isLoadingMoreApplications = true
                        searchViewModel.loadMore(context!!)
                    }
                } else {
                    loadMoreContainer.visibility = View.GONE
                }
            }
        })

        // Bind search view suggestions adapter to search suggestions list in view model
        searchViewModel.getSuggestions().observe(this, Observer {
            populateSuggestionsAdapter(it)
        })

        // Bind recycler view adapter to search results list in view model
        searchViewModel.getApplications().observe(this, Observer {
            if (it != null) {
                applicationList.clear()
                applicationList.addAll(it)
                progressBar.visibility = View.GONE
                recyclerView.adapter?.notifyDataSetChanged()
                if (!isLoadingMoreApplications) {
                    recyclerView.scrollToPosition(0)
                }
                loadMoreContainer.visibility = View.GONE
                isLoadingMoreApplications = false
                if (applicationList.isEmpty()) {
                    recyclerView.visibility = View.GONE
                } else {
                    recyclerView.visibility = View.VISIBLE
                }
            }
        })

        // Bind to the screen error
        searchViewModel.getScreenError().observe(this, Observer {
            if (it != null) {
                if (!isLoadingMoreApplications) {
                    applicationList.clear()
                    errorDescription.text = activity!!.getString(it.description)
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

        // Handle suggestion clicks
        searchView.setOnSuggestionListener(this)

        // Handle search queries
        searchView.setOnQueryTextListener(this)

        configureCloseButton(searchView)


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



    private fun configureCloseButton(searchView: SearchView) {

        val searchClose =  searchView.javaClass.getDeclaredField("mCloseButton")
        searchClose.isAccessible = true
        val closeImage = searchClose.get(searchView) as ImageView
        closeImage.setImageResource(R.drawable.ic_close_button) // your image here
    }

    override fun onSuggestionSelect(position: Int): Boolean {
        return true
    }

    override fun onSuggestionClick(position: Int): Boolean {
        searchViewModel.getSuggestions().value?.let {
            searchView.setQuery(it[position], true)
        }
        return true
    }

    private fun populateSuggestionsAdapter(suggestions: ArrayList<String>?) {
        val cursor = MatrixCursor(arrayOf(BaseColumns._ID, SUGGESTION_KEY))
        if (suggestions != null) {
            for (i in 0 until suggestions.size) {
                cursor.addRow(arrayOf(i, suggestions[i]))
            }
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
        focusView?.requestFocus()
        if (::searchViewModel.isInitialized) {
            searchViewModel.getApplications().value?.let {
                it.forEach { application ->
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
