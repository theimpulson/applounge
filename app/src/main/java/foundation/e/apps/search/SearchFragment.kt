/*
 * Apps  Quickly and easily install Android apps onto your device!
 * Copyright (C) 2021  E FOUNDATION
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

package foundation.e.apps.search

import android.app.Activity
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.AuthData
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.MainActivityViewModel
import foundation.e.apps.R
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.applicationlist.model.ApplicationListRVAdapter
import foundation.e.apps.databinding.FragmentSearchBinding
import foundation.e.apps.utils.pkg.PkgManagerModule
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment :
    Fragment(R.layout.fragment_search),
    SearchView.OnQueryTextListener,
    SearchView.OnSuggestionListener,
    FusedAPIInterface {

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var pkgManagerModule: PkgManagerModule

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val searchViewModel: SearchViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    private val TAG = SearchFragment::class.java.simpleName
    private val SUGGESTION_KEY = "suggestion"

    private var searchView: SearchView? = null
    private var progressBar: ProgressBar? = null
    private var recyclerView: RecyclerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        searchView = binding.searchView
        progressBar = binding.progressBar
        recyclerView = binding.recyclerView

        // Setup SearchView
        setHasOptionsMenu(true)
        searchView?.setOnSuggestionListener(this)
        searchView?.setOnQueryTextListener(this)
        searchView?.let { configureCloseButton(it) }

        // Setup SearchView Suggestions
        val from = arrayOf(SUGGESTION_KEY)
        val to = intArrayOf(android.R.id.text1)
        searchView?.suggestionsAdapter = SimpleCursorAdapter(
            context,
            R.layout.custom_simple_list_item, null, from, to,
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )

        searchViewModel.searchSuggest.observe(viewLifecycleOwner, {
            it?.let { populateSuggestionsAdapter(it) }
        })

        // Setup Search Results
        val listAdapter =
            findNavController().currentDestination?.id?.let { ApplicationListRVAdapter(this, it, pkgManagerModule) }
        recyclerView?.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(view.context)
        }

        searchViewModel.searchResult.observe(viewLifecycleOwner, {
            listAdapter?.setData(it)
            progressBar?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE
        })
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let { text ->
            hideKeyboard(activity as Activity)
            view?.requestFocus()
            progressBar?.visibility = View.VISIBLE
            recyclerView?.visibility = View.GONE
            val data = mainActivityViewModel.authData.value?.let {
                gson.fromJson(
                    it,
                    AuthData::class.java
                )
            }
            data?.let { searchViewModel.getSearchResults(text, it) }
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let { text ->
            val data = mainActivityViewModel.authData.value?.let {
                gson.fromJson(
                    it,
                    AuthData::class.java
                )
            }
            data?.let { searchViewModel.getSearchSuggestions(text, it) }
        }
        return true
    }

    override fun onSuggestionSelect(position: Int): Boolean {
        return true
    }

    override fun onSuggestionClick(position: Int): Boolean {
        searchViewModel.searchSuggest.value?.let {
            searchView?.setQuery(it[position].suggestedQuery, true)
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        searchView = null
        progressBar = null
        recyclerView = null
    }

    private fun configureCloseButton(searchView: SearchView) {
        val searchClose = searchView.javaClass.getDeclaredField("mCloseButton")
        searchClose.isAccessible = true
        val closeImage = searchClose.get(searchView) as ImageView
        closeImage.setImageResource(R.drawable.ic_close)
    }

    private fun hideKeyboard(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun populateSuggestionsAdapter(suggestions: List<SearchSuggestEntry>?) {
        val cursor = MatrixCursor(arrayOf(BaseColumns._ID, SUGGESTION_KEY))
        suggestions?.let {
            for (i in it.indices) {
                cursor.addRow(arrayOf(i, it[i].suggestedQuery))
            }
        }
        searchView?.suggestionsAdapter?.changeCursor(cursor)
    }

    override fun getApplication(
        id: String,
        name: String,
        packageName: String,
        versionCode: Int,
        offerType: Int?,
        origin: Origin?
    ) {
        val data = mainActivityViewModel.authData.value?.let {
            gson.fromJson(
                it,
                AuthData::class.java
            )
        }
        val offer = offerType ?: 0
        val org = origin ?: Origin.CLEANAPK
        data?.let {
            searchViewModel.getApplication(
                id,
                name,
                packageName,
                versionCode,
                offer,
                it,
                org
            )
        }
    }
}
