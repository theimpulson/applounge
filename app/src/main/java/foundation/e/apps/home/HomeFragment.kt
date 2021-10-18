package foundation.e.apps.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.R
import foundation.e.apps.databinding.FragmentHomeBinding
import foundation.e.apps.home.model.HomeFeaturedRVAdapter
import foundation.e.apps.home.model.HomeRVAdapter

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()
    private val TAG = HomeFragment::class.java.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        val featuredRV = binding.featuredRV

        // Fetch data to display
        homeViewModel.getHomeScreenData()

        // Setup adapters
        val featuredAdapter = HomeFeaturedRVAdapter()
        val topUpdatedAppsAdapter = HomeRVAdapter()
        val topUpdatedGamesAdapter = HomeRVAdapter()
        val top24AppsAdapter = HomeRVAdapter()
        val top24GamesAdapter = HomeRVAdapter()
        val discoverAdapter = HomeRVAdapter()

        // Setup SnapHelper with FeaturedRV to limit scrolling to 1 item
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(featuredRV)

        // Setup recycler views
        featuredRV.apply {
            adapter = featuredAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
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
            featuredAdapter.setData(it.home.banner_apps)
            topUpdatedAppsAdapter.setData(it.home.top_updated_apps)
            topUpdatedGamesAdapter.setData(it.home.top_updated_games)
            top24AppsAdapter.setData(it.home.popular_apps_in_last_24_hours)
            top24GamesAdapter.setData(it.home.popular_games_in_last_24_hours)
            discoverAdapter.setData(it.home.discover)

            // Remove progress bars
            binding.featuredPB.visibility = View.GONE
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
}
