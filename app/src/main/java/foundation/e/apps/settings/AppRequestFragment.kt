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
            setTitle(R.string.preference_apps_request_app_title)
            setNavigationOnClickListener {
                view.findNavController().navigate(R.id.settingsFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
