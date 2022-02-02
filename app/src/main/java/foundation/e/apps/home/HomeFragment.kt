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

package foundation.e.apps.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.MainActivityViewModel
import foundation.e.apps.R
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.databinding.FragmentHomeBinding
import foundation.e.apps.home.model.HomeParentRVAdapter
import foundation.e.apps.manager.pkg.PkgManagerModule
import foundation.e.apps.utils.enums.User
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), FusedAPIInterface {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    @Inject
    lateinit var pkgManagerModule: PkgManagerModule

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        mainActivityViewModel.authData.observe(viewLifecycleOwner) {
            homeViewModel.getHomeScreenData(it)
        }

        val homeParentRVAdapter = HomeParentRVAdapter(
            this,
            pkgManagerModule,
            User.valueOf(mainActivityViewModel.userType.value ?: User.UNAVAILABLE.name)
        )

        binding.parentRV.apply {
            adapter = homeParentRVAdapter
            layoutManager = LinearLayoutManager(view.context)
        }

        mainActivityViewModel.downloadList.observe(viewLifecycleOwner) { list ->
            val homeList = homeViewModel.homeScreenData.value?.toMutableList()
            if (!homeList.isNullOrEmpty()) {
                homeList.forEach { home ->
                    list.forEach {
                        home.list.find { app ->
                            app.origin == it.origin && (app.package_name == it.package_name || app._id == it.id)
                        }?.status = it.status
                    }
                }
                homeViewModel.homeScreenData.value = homeList
            }
        }

        homeViewModel.homeScreenData.observe(viewLifecycleOwner) {
            homeParentRVAdapter.setData(it)
            binding.shimmerLayout.visibility = View.GONE
            binding.parentRV.visibility = View.VISIBLE
        }
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

    override fun getApplication(app: FusedApp) {
        mainActivityViewModel.authData.value?.let {
            homeViewModel.getApplication(it, app)
        }
    }

    override fun cancelDownload(app: FusedApp) {
        homeViewModel.cancelDownload(app.package_name)
    }
}
