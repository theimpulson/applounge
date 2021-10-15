package foundation.e.apps.applicationlist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import foundation.e.apps.R
import foundation.e.apps.databinding.FragmentApplicationListBinding

class ApplicationListFragment : Fragment(R.layout.fragment_application_list) {
    private lateinit var binding: FragmentApplicationListBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentApplicationListBinding.bind(view)
    }
}
