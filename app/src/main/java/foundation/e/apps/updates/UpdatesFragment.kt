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
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.applicationlist.model.ApplicationListRVAdapter
import foundation.e.apps.databinding.FragmentUpdatesBinding
import foundation.e.apps.manager.pkg.PkgManagerModule
import foundation.e.apps.utils.enums.User
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

        mainActivityViewModel.authData.observe(viewLifecycleOwner) { date ->
            updatesViewModel.getUpdates(date)
            binding.button.setOnClickListener {
                updatesViewModel.updateAllApps(date)
            }
        }

        val recyclerView = binding.recyclerView
        val listAdapter = findNavController().currentDestination?.id?.let {
            ApplicationListRVAdapter(
                this,
                it,
                pkgManagerModule,
                User.valueOf(mainActivityViewModel.userType.value ?: User.UNAVAILABLE.name)
            )
        }

        recyclerView.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(view.context)
        }

        mainActivityViewModel.downloadList.observe(viewLifecycleOwner) { list ->
            val updatesList = updatesViewModel.updatesList.value?.toMutableList()
            if (!updatesList.isNullOrEmpty()) {
                list.forEach {
                    updatesList.find { app ->
                        app.origin == it.origin && (app.package_name == it.package_name || app._id == it.id)
                    }?.status = it.status
                }
                updatesViewModel.updatesList.value = updatesList
            }
        }

        updatesViewModel.updatesList.observe(viewLifecycleOwner) {
            listAdapter?.setData(it)
            binding.progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            if (!it.isNullOrEmpty()) binding.button.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getApplication(app: FusedApp) {
        mainActivityViewModel.authData.value?.let {
            updatesViewModel.getApplication(it, app)
        }
    }

    override fun cancelDownload(app: FusedApp) {
        updatesViewModel.cancelDownload(app.package_name)
    }
}
