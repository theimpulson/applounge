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

package foundation.e.apps.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aurora.gplayapi.data.models.Category
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.MainActivityViewModel
import foundation.e.apps.R
import foundation.e.apps.categories.model.CategoriesRVAdapter
import foundation.e.apps.databinding.FragmentAppsBinding
import foundation.e.apps.utils.enums.ResultStatus
import foundation.e.apps.utils.interfaces.TimeoutFragment

@AndroidEntryPoint
class AppsFragment : Fragment(R.layout.fragment_apps), TimeoutFragment {
    private var _binding: FragmentAppsBinding? = null
    private val binding get() = _binding!!

    private val categoriesViewModel: CategoriesViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAppsBinding.bind(view)

        mainActivityViewModel.internetConnection.observe(viewLifecycleOwner) { hasInternet ->
            mainActivityViewModel.authData.value?.let { authData ->
                if (hasInternet) {
                    categoriesViewModel.getCategoriesList(
                        Category.Type.APPLICATION,
                        authData
                    )
                }
            }

            val categoriesRVAdapter = CategoriesRVAdapter()
            val recyclerView = binding.recyclerView

            recyclerView.apply {
                adapter = categoriesRVAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                visibility = View.GONE
            }

            categoriesViewModel.categoriesList.observe(viewLifecycleOwner) {
                categoriesRVAdapter.setData(it.first)
                binding.shimmerLayout.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                if (it.third != ResultStatus.OK) {
                    onTimeout()
                }
            }
        }
    }

    override fun onTimeout() {
        if (!mainActivityViewModel.isTimeoutDialogDisplayed()) {
            mainActivityViewModel.displayTimeoutAlertDialog(
                activity = requireActivity(),
                message = getString(R.string.timeout_desc_cleanapk),
                positiveButtonText = if (!categoriesViewModel.isCategoriesEmpty()) {
                    /*
                     * It may happen that both GPlay and cleanapk is unreachable.
                     * In that case of user presses OK to dismiss the dialog,
                     * only blank screen will be shown.
                     */
                    getString(android.R.string.ok)
                } else null,
                positiveButtonBlock = {},
                negativeButtonText = getString(R.string.retry),
                negativeButtonBlock = {
                    showLoadingShimmer()
                    mainActivityViewModel.retryFetchingTokenAfterTimeout()
                },
                allowCancel = !categoriesViewModel.isCategoriesEmpty(),
            )
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
