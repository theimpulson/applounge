package foundation.e.apps.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import foundation.e.apps.R
import foundation.e.apps.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
    }

}