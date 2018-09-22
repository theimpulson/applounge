package io.eelo.appinstaller.search

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import io.eelo.appinstaller.R
import io.eelo.appinstaller.Settings
import io.eelo.appinstaller.application.ApplicationManager

class SearchFragment : Fragment(), SearchView.OnQueryTextListener {

    private var settings: Settings? = null
    private var searchViewModel : SearchModel? = null
    private var searchView : SearchView? = null
    private var recyclerView : RecyclerView? = null

    companion object {
        fun newInstance(settings: Settings): SearchFragment {
            val fragment = SearchFragment()
            fragment.settings = settings
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        searchViewModel = SearchModel(settings!!)

        searchView = view.findViewById(R.id.search_view)
        recyclerView = view.findViewById(R.id.app_list)

        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        refreshList(ArrayList())

        searchView!!.setOnQueryTextListener(this)

        return view
    }

    private fun refreshList(apps: List<ApplicationManager>) {
        recyclerView!!.adapter = ApplicationListAdapter(apps)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        val apps = searchViewModel!!.search(query!!).apps
        refreshList(apps)
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }
}
