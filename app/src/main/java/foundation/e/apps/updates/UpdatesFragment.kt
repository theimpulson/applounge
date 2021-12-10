/*
 * Apps  Quickly and easily install Android apps onto your device!
 * Copyright (C) 2021  E FOUNDATION
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.updates

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.MainActivityViewModel
import foundation.e.apps.R
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.applicationlist.model.ApplicationListRVAdapter
import foundation.e.apps.databinding.FragmentUpdatesBinding
import foundation.e.apps.utils.pkg.PkgManagerModule
import javax.inject.Inject

@AndroidEntryPoint
class UpdatesFragment : Fragment(R.layout.fragment_updates), FusedAPIInterface {

    private var _binding: FragmentUpdatesBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var pkgManagerModule: PkgManagerModule

    private val updatesViewModel: UpdatesViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUpdatesBinding.bind(view)

        binding.button.isEnabled = false

        mainActivityViewModel.authData.observe(viewLifecycleOwner, {
            updatesViewModel.getUpdates(it)
        })

        val recyclerView = binding.recyclerView
        val listAdapter =
            findNavController().currentDestination?.id?.let {
                ApplicationListRVAdapter(
                    this,
                    it,
                    pkgManagerModule
                )
            }
        recyclerView.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(view.context)
        }

        updatesViewModel.updatesList.observe(viewLifecycleOwner, {
            listAdapter?.setData(it)
            binding.progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getApplication(
        id: String,
        name: String,
        packageName: String,
        versionCode: Int,
        offerType: Int?,
        origin: Origin?
    ) {
        val offer = offerType ?: 0
        val org = origin ?: Origin.CLEANAPK
        mainActivityViewModel.authData.value?.let {
            updatesViewModel.getApplication(
                id,
                name,
                packageName,
                versionCode,
                offer,
                it,
                org
            )
        }
    }
}
