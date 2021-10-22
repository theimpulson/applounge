package foundation.e.apps.categories

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.R
import foundation.e.apps.categories.model.CategoriesRVAdapter
import foundation.e.apps.categories.model.CategoriesVPAdapter
import foundation.e.apps.databinding.FragmentCategoriesBinding

@AndroidEntryPoint
class CategoriesFragment : Fragment(R.layout.fragment_categories) {
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val TAG = CategoriesFragment::class.java.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCategoriesBinding.bind(view)

        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager
        viewPager.adapter = CategoriesVPAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.apps)
                1 -> tab.text = getString(R.string.games)
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
