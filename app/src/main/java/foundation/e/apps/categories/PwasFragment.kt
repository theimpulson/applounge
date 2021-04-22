/*
 * Copyright (C) 2019-2021  E FOUNDATION
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

package foundation.e.apps.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import foundation.e.apps.categories.viewmodel.CategoriesViewModel
import foundation.e.apps.databinding.FragmentApplicationCategoriesBinding

class PwasFragment : Fragment() {
    private var _binding: FragmentApplicationCategoriesBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoriesViewModel: CategoriesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentApplicationCategoriesBinding.inflate(inflater, container, false)

        categoriesViewModel = ViewModelProvider(this).get(CategoriesViewModel::class.java)

        // Fragment variables
        val categoriesList = binding.categoriesList
        val progressBar = binding.progressBar
        val errorContainer = binding.errorLayout.errorContainer
        val errorResolve = binding.errorLayout.errorResolve
        val errorDescription = binding.errorLayout.errorDescription

        categoriesList.layoutManager = LinearLayoutManager(context)

        categoriesList.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        errorContainer.visibility = View.GONE
        errorResolve.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            categoriesViewModel.loadCategories(requireContext())
        }

        // Bind to the list of pwas categories
        categoriesViewModel.getPwasCategories().observe(viewLifecycleOwner, Observer {
            if (it!!.isNotEmpty()) {
                categoriesList.adapter = context?.let { context -> CategoriesListAdapter(context, it, null) }
                categoriesList.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
        })

        // Bind to the screen error
        categoriesViewModel.getScreenError().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                errorDescription.text = requireActivity().getString(it.description)
                errorContainer.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            } else {
                errorContainer.visibility = View.GONE
            }
        })

        if (categoriesViewModel.getPwasCategories().value!!.isEmpty()) {
            categoriesViewModel.loadCategories(requireContext())
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
