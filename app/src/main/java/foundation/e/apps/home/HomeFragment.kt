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
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
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

        mainActivityViewModel.userType.observe(viewLifecycleOwner) { user ->
            if (user.isNullOrEmpty() || User.valueOf(user) == User.UNAVAILABLE) {
                mainActivityViewModel.tocStatus.observe(viewLifecycleOwner) { tosAccepted ->
                    onTosAccepted(tosAccepted)
                }
            }
        }

        mainActivityViewModel.internetConnection.observe(viewLifecycleOwner) { hasInternet ->
            mainActivityViewModel.authData.observe(viewLifecycleOwner) { authData ->
                if (hasInternet) {
                    authData?.let {
                        homeViewModel.getHomeScreenData(authData)
                    }
                }
            }
        }

        val homeParentRVAdapter = HomeParentRVAdapter(
            this,
            pkgManagerModule,
            User.valueOf(mainActivityViewModel.userType.value ?: User.UNAVAILABLE.name),
            mainActivityViewModel, viewLifecycleOwner
        )

        binding.parentRV.apply {
            adapter = homeParentRVAdapter
            layoutManager = LinearLayoutManager(view.context)
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

    override fun getApplication(app: FusedApp, appIcon: ImageView?) {
        mainActivityViewModel.getApplication(app, appIcon)
    }

    override fun cancelDownload(app: FusedApp) {
        mainActivityViewModel.cancelDownload(app)
    }

    private fun onTosAccepted(isTosAccepted: Boolean) {
        if (isTosAccepted) {
            view?.findNavController()
                ?.navigate(R.id.action_homeFragment_to_signInFragment)
        }
    }
}
