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
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.AuthData
import com.facebook.shimmer.ShimmerFrameLayout
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.AppInfoFetchViewModel
import foundation.e.apps.AppProgressViewModel
import foundation.e.apps.MainActivityViewModel
import foundation.e.apps.PrivacyInfoViewModel
import foundation.e.apps.R
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.application.subFrags.ApplicationDialogFragment
import foundation.e.apps.applicationlist.model.ApplicationListRVAdapter
import foundation.e.apps.databinding.FragmentSearchBinding
import foundation.e.apps.manager.pkg.PkgManagerModule
import foundation.e.apps.utils.enums.ResultStatus
import foundation.e.apps.utils.enums.Status
import foundation.e.apps.utils.enums.User
import foundation.e.apps.utils.interfaces.TimeoutFragmentInterface
import foundation.e.apps.utils.modules.PWAManagerModule
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment :
    Fragment(R.layout.fragment_search),
    SearchView.OnQueryTextListener,
    SearchView.OnSuggestionListener,
    FusedAPIInterface,
    TimeoutFragmentInterface {

    @Inject
    lateinit var pkgManagerModule: PkgManagerModule

    @Inject
    lateinit var pwaManagerModule: PWAManagerModule

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val searchViewModel: SearchViewModel by viewModels()
    private val privacyInfoViewModel: PrivacyInfoViewModel by viewModels()
    private val appInfoFetchViewModel: AppInfoFetchViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    private val appProgressViewModel: AppProgressViewModel by viewModels()

    private val SUGGESTION_KEY = "suggestion"

    private var searchView: SearchView? = null
    private var shimmerLayout: ShimmerFrameLayout? = null
    private var recyclerView: RecyclerView? = null
    private var searchHintLayout: LinearLayout? = null
    private var noAppsFoundLayout: LinearLayout? = null

    /*
     * Store the string from onQueryTextSubmit() and access it from refreshData()
     * Issue: https://gitlab.e.foundation/e/backlog/-/issues/5413
     */
    private var searchText = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        searchView = binding.searchView
        shimmerLayout = binding.shimmerLayout
        recyclerView = binding.recyclerView
        searchHintLayout = binding.searchHintLayout.root
        noAppsFoundLayout = binding.noAppsFoundLayout.root

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

        searchViewModel.searchSuggest.observe(viewLifecycleOwner) {
            it?.let { populateSuggestionsAdapter(it) }
        }

        // Setup Search Results
        val listAdapter = findNavController().currentDestination?.id?.let {
            ApplicationListRVAdapter(
                this,
                privacyInfoViewModel,
                appInfoFetchViewModel,
                mainActivityViewModel,
                it,
                pkgManagerModule,
                pwaManagerModule,
                User.valueOf(mainActivityViewModel.userType.value ?: User.UNAVAILABLE.name),
                viewLifecycleOwner
            ) { fusedApp ->
                if (!mainActivityViewModel.shouldShowPaidAppsSnackBar(fusedApp)) {
                    ApplicationDialogFragment(
                        title = getString(R.string.dialog_title_paid_app, fusedApp.name),
                        message = getString(
                            R.string.dialog_paidapp_message,
                            fusedApp.name,
                            fusedApp.price
                        ),
                        positiveButtonText = getString(R.string.dialog_confirm),
                        positiveButtonAction = {
                            getApplication(fusedApp)
                        },
                        cancelButtonText = getString(R.string.dialog_cancel),
                    ).show(childFragmentManager, "SearchFragment")
                }
            }
        }

        recyclerView?.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(view.context)
        }

        appProgressViewModel.downloadProgress.observe(viewLifecycleOwner) {
            val adapter = recyclerView?.adapter as ApplicationListRVAdapter
            lifecycleScope.launch {
                adapter.currentList.forEach { fusedApp ->
                    if (fusedApp.status == Status.DOWNLOADING) {
                        val progress = appProgressViewModel.calculateProgress(fusedApp, it)
                        val downloadProgress =
                            ((progress.second / progress.first.toDouble()) * 100).toInt()
                        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(
                            adapter.currentList.indexOf(fusedApp)
                        )
                        viewHolder?.let {
                            (viewHolder as ApplicationListRVAdapter.ViewHolder).binding.installButton.text =
                                "$downloadProgress%"
                        }
                    }
                }
            }
        }

        mainActivityViewModel.downloadList.observe(viewLifecycleOwner) { list ->
            val searchList = searchViewModel.searchResult.value?.first?.toMutableList() ?: emptyList()
            searchList.let {
                mainActivityViewModel.updateStatusOfFusedApps(searchList, list)
            }

            /*
             * Done in one line, so that on Ctrl+click on searchResult,
             * we can see that it is being updated here.
             */
            searchViewModel.searchResult.apply { value = Pair(searchList, value?.second) }
        }


        /*
         * Explanation of double observers in HomeFragment.kt
         * Modified to check and search only if searchText in not blank, to prevent blank search.
         */

        mainActivityViewModel.internetConnection.observe(viewLifecycleOwner) {
            if (searchText.isNotBlank()) {
                refreshDataOrRefreshToken(mainActivityViewModel)
            }
        }
        mainActivityViewModel.authData.observe(viewLifecycleOwner) {
            if (searchText.isNotBlank()) {
                refreshDataOrRefreshToken(mainActivityViewModel)
            }
        }

        searchViewModel.searchResult.observe(viewLifecycleOwner) {
            if (it.first.isNullOrEmpty()) {
                noAppsFoundLayout?.visibility = View.VISIBLE
            } else {
                listAdapter?.setData(it.first)
                shimmerLayout?.visibility = View.GONE
                recyclerView?.visibility = View.VISIBLE
                noAppsFoundLayout?.visibility = View.GONE
            }
            if (searchText.isNotBlank() && it.second != ResultStatus.OK) {
                /*
                 * If blank check is not performed then timeout dialog keeps
                 * popping up whenever search tab is opened.
                 */
                onTimeout()
            }
        }
    }

    override fun onTimeout() {
        if (!mainActivityViewModel.isTimeoutDialogDisplayed()) {
            mainActivityViewModel.displayTimeoutAlertDialog(
                activity = requireActivity(),
                message = getString(R.string.timeout_desc_cleanapk),
                positiveButtonText = getString(R.string.retry),
                positiveButtonBlock = {
                    showLoadingShimmer()
                    mainActivityViewModel.retryFetchingTokenAfterTimeout()
                },
                negativeButtonText = getString(android.R.string.ok),
                negativeButtonBlock = {},
                allowCancel = true,
            )
        }
    }

    override fun refreshData(authData: AuthData) {
        searchViewModel.getSearchResults(searchText, authData)
    }

    private fun showLoadingShimmer() {
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        binding.shimmerLayout.startShimmer()
    }

    override fun onPause() {
        binding.shimmerLayout.stopShimmer()
        super.onPause()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let { text ->
            hideKeyboard(activity as Activity)
            view?.requestFocus()
            searchHintLayout?.visibility = View.GONE
            shimmerLayout?.visibility = View.VISIBLE
            recyclerView?.visibility = View.GONE
            noAppsFoundLayout?.visibility = View.GONE
            /*
             * Set the search text and call for network result.
             */
            searchText = text
            refreshDataOrRefreshToken(mainActivityViewModel)
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let { text ->
            mainActivityViewModel.authData.value?.let {
                searchViewModel.getSearchSuggestions(text, it)
            }
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
        shimmerLayout = null
        recyclerView = null
        searchHintLayout = null
        noAppsFoundLayout = null
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

    override fun getApplication(app: FusedApp, appIcon: ImageView?) {
        mainActivityViewModel.getApplication(app, appIcon)
    }

    override fun cancelDownload(app: FusedApp) {
        mainActivityViewModel.cancelDownload(app)
    }
}
