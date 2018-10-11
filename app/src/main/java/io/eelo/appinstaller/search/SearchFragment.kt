package io.eelo.appinstaller.search

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.CursorAdapter
import android.support.v4.widget.SimpleCursorAdapter
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.eelo.appinstaller.R
import android.provider.BaseColumns
import android.database.MatrixCursor
import io.eelo.appinstaller.common.ApplicationListAdapter
import io.eelo.appinstaller.search.viewModel.SearchViewModel

class SearchFragment : Fragment(), SearchView.OnQueryTextListener, SearchView.OnSuggestionListener {
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var searchView: SearchView
    private val SUGGESTION_KEY = "suggestion"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchViewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        searchViewModel.initialise(context!!)
        searchView = view.findViewById(R.id.search_view)
        val recyclerView = view.findViewById<RecyclerView>(R.id.app_list)

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
        recyclerView.adapter = ApplicationListAdapter(context!!, searchViewModel.getApplications().value!!)

        // Bind search view suggestions adapter to search suggestions list in view model
        searchViewModel.getSuggestions().observe(this, Observer {
            populateSuggestionsAdapter(it!!)
        })

        // Bind recycler view adapter to search results list in view model
        searchViewModel.getApplications().observe(this, Observer {
            recyclerView.adapter.notifyDataSetChanged()
        })

        // Handle suggestion clicks
        searchView.setOnSuggestionListener(this)

        // Handle search queries
        searchView.setOnQueryTextListener(this)

        return view
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let {
            searchViewModel.onSearchQuerySubmitted(it)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        searchView.query?.let {
            searchViewModel.onSearchQueryChanged(it.toString())
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
}
