package io.eelo.appinstaller.search

import android.databinding.ObservableList
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.Application
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        val searchViewModel = SearchViewModel()

        val searchView = view.findViewById<SearchView>(R.id.search_view)
        val recyclerView = view.findViewById<RecyclerView>(R.id.app_list)
        val viewManager = LinearLayoutManager(context)

        // Initialise recycler view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = viewManager

        // Bind recycler view adapter to search results list in view model
        searchViewModel.applicationList.addOnListChangedCallback(object : ObservableList.OnListChangedCallback<ObservableList<Application>>() {
            override fun onChanged(sender: ObservableList<Application>) {

            }

            override fun onItemRangeChanged(sender: ObservableList<Application>, positionStart: Int, itemCount: Int) {

            }

            override fun onItemRangeInserted(sender: ObservableList<Application>, positionStart: Int, itemCount: Int) {
                recyclerView.adapter = ApplicationListAdapter(searchViewModel.applicationList)
            }

            override fun onItemRangeMoved(sender: ObservableList<Application>, fromPosition: Int, toPosition: Int, itemCount: Int) {

            }

            override fun onItemRangeRemoved(sender: ObservableList<Application>, positionStart: Int, itemCount: Int) {

            }
        })

        // Handle search queries
        searchView.setOnSearchClickListener {
            searchViewModel.onSearchClick(searchView.query.toString())
        }

        return view
    }
}
