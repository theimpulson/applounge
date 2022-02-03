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

package foundation.e.apps.applicationlist

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.MainActivityViewModel
import foundation.e.apps.R
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.applicationlist.model.ApplicationListRVAdapter
import foundation.e.apps.databinding.FragmentApplicationListBinding
import foundation.e.apps.manager.pkg.PkgManagerModule
import foundation.e.apps.utils.enums.User
import javax.inject.Inject

@AndroidEntryPoint
class ApplicationListFragment : Fragment(R.layout.fragment_application_list), FusedAPIInterface {

    private val args: ApplicationListFragmentArgs by navArgs()

    @Inject
    lateinit var pkgManagerModule: PkgManagerModule

    private val viewModel: ApplicationListViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    private var _binding: FragmentApplicationListBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentApplicationListBinding.bind(view)

        mainActivityViewModel.authData.value?.let {
            viewModel.getList(
                args.category,
                args.browseUrl,
                it,
                args.source
            )
        }

        binding.toolbarTitleTV.text = args.translation
        binding.toolbar.apply {
            setNavigationOnClickListener {
                view.findNavController().navigate(R.id.categoriesFragment)
            }
        }

        val recyclerView = binding.recyclerView
        val listAdapter =
            findNavController().currentDestination?.id?.let {
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
            val categoryList = viewModel.appListLiveData.value?.toMutableList()
            if (!categoryList.isNullOrEmpty()) {
                list.forEach {
                    categoryList.find { app ->
                        app.origin == it.origin && (app.package_name == it.package_name || app._id == it.id)
                    }?.status = it.status
                }
                viewModel.appListLiveData.value = categoryList
            }
        }

        viewModel.appListLiveData.observe(viewLifecycleOwner) {
            listAdapter?.setData(it)
            binding.shimmerLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        binding.shimmerLayout.startShimmer()
    }

    override fun onPause() {
        binding.shimmerLayout.stopShimmer()
        super.onPause()
    }

    override fun getApplication(app: FusedApp, appIcon: ImageView?) {
        mainActivityViewModel.getApplication(app, appIcon)
    }

    override fun cancelDownload(app: FusedApp) {
        mainActivityViewModel.cancelDownload(app)
    }
}
