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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.aurora.gplayapi.data.models.AuthData
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.MainActivityViewModel
import foundation.e.apps.R
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.applicationlist.model.ApplicationListRVAdapter
import foundation.e.apps.databinding.FragmentApplicationListBinding
import foundation.e.apps.utils.pkg.PkgManagerModule
import javax.inject.Inject

@AndroidEntryPoint
class ApplicationListFragment : Fragment(R.layout.fragment_application_list), FusedAPIInterface {

    private val args: ApplicationListFragmentArgs by navArgs()
    private val TAG = ApplicationListFragment::class.java.simpleName

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var pkgManagerModule: PkgManagerModule

    private val applicationListViewModel: ApplicationListViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    private var _binding: FragmentApplicationListBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentApplicationListBinding.bind(view)

        // Set title
        binding.toolbar.apply {
            title = args.translation
            setNavigationOnClickListener {
                view.findNavController().navigate(R.id.categoriesFragment)
            }
        }

        val recyclerView = binding.recyclerView
        val listAdapter =
            findNavController().currentDestination?.id?.let { ApplicationListRVAdapter(this, it, pkgManagerModule) }
        recyclerView.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(view.context)
        }

        applicationListViewModel.getList(args.category)
        applicationListViewModel.list.observe(viewLifecycleOwner, {
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
        val data = mainActivityViewModel.authData.value?.let {
            gson.fromJson(
                it,
                AuthData::class.java
            )
        }
        val offer = offerType ?: 0
        val org = origin ?: Origin.CLEANAPK
        data?.let {
            applicationListViewModel.getApplication(
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
