package foundation.e.apps.home

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.R
import foundation.e.apps.databinding.FragmentHomeBinding

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        // TODO: Get rid of code below
        homeViewModel.searchApp()
        homeViewModel.myResponse.observe(viewLifecycleOwner, {
            if (it.isSuccessful && it.body() != null) {
                Log.d("HomeFragment", it.body()!!.apps.toString())
            }
        })
    }

}