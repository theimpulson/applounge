package foundation.e.apps.application

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import foundation.e.apps.R
import foundation.e.apps.databinding.FragmentApplicationBinding

class ApplicationFragment : Fragment(R.layout.fragment_application) {
    private lateinit var binding: FragmentApplicationBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationBinding.bind(view)
    }
}
