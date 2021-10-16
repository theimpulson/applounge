package foundation.e.apps.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.R
import foundation.e.apps.databinding.FragmentGamesBinding

@AndroidEntryPoint
class GamesFragment : Fragment(R.layout.fragment_games) {

    private var _binding: FragmentGamesBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGamesBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
