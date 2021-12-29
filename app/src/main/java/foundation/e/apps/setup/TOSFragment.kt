package foundation.e.apps.setup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import foundation.e.apps.R
import foundation.e.apps.databinding.FragmentTosBinding

class TOSFragment : Fragment(R.layout.fragment_tos) {

    private var _binding: FragmentTosBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTosBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}