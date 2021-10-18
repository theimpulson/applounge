package foundation.e.apps.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.R
import foundation.e.apps.databinding.FragmentAppRequestBinding

@AndroidEntryPoint
class AppRequestFragment : Fragment(R.layout.fragment_app_request) {

    private var _binding: FragmentAppRequestBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAppRequestBinding.bind(view)

        // Set title
        binding.toolbar.apply {
            setNavigationOnClickListener {
                view.findNavController().navigate(R.id.settingsFragment)
            }
        }
        binding.button.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
