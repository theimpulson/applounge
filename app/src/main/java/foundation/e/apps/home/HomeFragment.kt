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
import androidx.recyclerview.widget.PagerSnapHelper
import com.aurora.gplayapi.data.models.AuthData
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.MainActivityViewModel
import foundation.e.apps.R
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.databinding.FragmentHomeBinding
import foundation.e.apps.home.model.HomeFeaturedRVAdapter
import foundation.e.apps.home.model.HomeRVAdapter
import foundation.e.apps.utils.PreferenceManagerModule
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), FusedAPIInterface {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var preferenceManagerModule: PreferenceManagerModule

    @Inject
    lateinit var gson: Gson

    private val homeViewModel: HomeViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    private val TAG = HomeFragment::class.java.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        val featuredRV = binding.featuredRV

        // Fetch data to display
        homeViewModel.getHomeScreenData()

        // Setup adapters
        val featuredAdapter = HomeFeaturedRVAdapter()
        val topUpdatedAppsAdapter = HomeRVAdapter(this)
        val topUpdatedGamesAdapter = HomeRVAdapter(this)
        val top24AppsAdapter = HomeRVAdapter(this)
        val top24GamesAdapter = HomeRVAdapter(this)
        val discoverAdapter = HomeRVAdapter(this)

        // Featured items are only available in default option
        if (preferenceManagerModule.preferredApplicationType() == "any") {
            binding.featuredTV.visibility = View.VISIBLE
            binding.featuredLayout.visibility = View.VISIBLE

            // Setup SnapHelper with FeaturedRV to limit scrolling to 1 item
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(featuredRV)

            // Setup recycler views
            featuredRV.apply {
                adapter = featuredAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
        }

        binding.topUpdatedAppsRV.apply {
            adapter = topUpdatedAppsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.topUpdatedGamesRV.apply {
            adapter = topUpdatedGamesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.top24AppsRV.apply {
            adapter = top24AppsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.top24GamesRV.apply {
            adapter = top24GamesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.discoverRV.apply {
            adapter = discoverAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        homeViewModel.homeScreenData.observe(viewLifecycleOwner, {
            // Pass data to respective adapters
            if (preferenceManagerModule.preferredApplicationType() == "any") {
                featuredAdapter.setData(it.home.banner_apps)
            }
            topUpdatedAppsAdapter.setData(it.home.top_updated_apps)
            topUpdatedGamesAdapter.setData(it.home.top_updated_games)
            top24AppsAdapter.setData(it.home.popular_apps_in_last_24_hours)
            top24GamesAdapter.setData(it.home.popular_games_in_last_24_hours)
            discoverAdapter.setData(it.home.discover)

            // Remove progress bars
            if (preferenceManagerModule.preferredApplicationType() == "any") {
                binding.featuredPB.visibility = View.GONE
            }
            binding.topUpdatedAppsPB.visibility = View.GONE
            binding.topUpdatedGamesPB.visibility = View.GONE
            binding.top24AppsPB.visibility = View.GONE
            binding.top24GamesPB.visibility = View.GONE
            binding.discoverPB.visibility = View.GONE
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getApplication(
        id: String,
        name: String,
        packageName: String,
        versionCode: Int,
        offerType: Int?,
        origin: Origin?
    ) {
        val offer = offerType ?: 0
        val org = origin ?: Origin.CLEANAPK
        mainActivityViewModel.authData.value?.let {
            homeViewModel.getApplication(
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
