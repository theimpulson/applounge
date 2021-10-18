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
import foundation.e.apps.home.model.HomeVPAdapter
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()
    private val TAG = HomeFragment::class.java.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // Setup featured items
        val featuredRV = binding.featuredRV
        val featuredPB = binding.featuredPB
        val featuredListAdapter = HomeVPAdapter()
        val snapHelper = PagerSnapHelper()

        featuredRV.apply {
            adapter = featuredListAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        snapHelper.attachToRecyclerView(featuredRV)

        // Fetch data to display
        homeViewModel.getHomeScreenData()
        homeViewModel.homeScreenData.observe(viewLifecycleOwner, {
            featuredListAdapter.setData(it.home.banner_apps)
            featuredPB.visibility = View.GONE
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
