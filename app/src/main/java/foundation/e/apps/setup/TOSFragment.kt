package foundation.e.apps.setup

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import foundation.e.apps.R
import foundation.e.apps.databinding.FragmentTosBinding

class TOSFragment : Fragment(R.layout.fragment_tos) {

    private var _binding: FragmentTosBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTosBinding.bind(view)

        binding.disagreeBT.setOnClickListener {
            activity?.finish()
        }

        binding.agreeBT.setOnClickListener {
            view.findNavController().navigate(R.id.signInFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}