package foundation.e.apps.updates

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import foundation.e.apps.R
import foundation.e.apps.databinding.FragmentUpdatesBinding


class UpdatesFragment : Fragment(R.layout.fragment_updates) {
    private lateinit var binding: FragmentUpdatesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUpdatesBinding.bind(view)
    }
}