package foundation.e.apps.setup.tos

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.R
import foundation.e.apps.databinding.FragmentTosBinding

@AndroidEntryPoint
class TOSFragment : Fragment(R.layout.fragment_tos) {

    private var _binding: FragmentTosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TOSViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTosBinding.bind(view)

        viewModel.tocStatus.observe(viewLifecycleOwner, {
            if (it == true) {
                binding.TOSWarning.visibility = View.GONE
                binding.TOSButtons.visibility = View.GONE
                binding.toolbar.visibility = View.VISIBLE
            }
        })

        binding.toolbar.setNavigationOnClickListener {
            it.findNavController().navigateUp()
        }

        binding.disagreeBT.setOnClickListener {
            activity?.finish()
        }

        binding.agreeBT.setOnClickListener {
            viewModel.saveTOCStatus(true)
            view.findNavController().navigate(R.id.signInFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
