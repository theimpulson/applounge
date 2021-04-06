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
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import foundation.e.apps.R
import foundation.e.apps.categories.viewmodel.CategoriesViewModel
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_application_categories.view.*

class PwasFragment : Fragment() {
    private lateinit var categoriesViewModel: CategoriesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        categoriesViewModel = ViewModelProvider(this).get(CategoriesViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_application_categories, container, false)
        view.categories_list.layoutManager = LinearLayoutManager(context)

        view.categories_list.visibility = View.GONE
        view.progress_bar.visibility = View.VISIBLE
        view.error_container.visibility = View.GONE
        view.findViewById<TextView>(R.id.error_resolve).setOnClickListener {
            view.progress_bar.visibility = View.VISIBLE
            categoriesViewModel.loadCategories(requireContext())
        }

        // Bind to the list of pwas categories
        categoriesViewModel.getPwasCategories().observe(viewLifecycleOwner, Observer {
            if (it!!.isNotEmpty()) {
                view.categories_list.adapter = CategoriesListAdapter(it, null)
                view.categories_list.visibility = View.VISIBLE
                view.progress_bar.visibility = View.GONE
            }
        })

        // Bind to the screen error
        categoriesViewModel.getScreenError().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                view.error_description.text = requireActivity().getString(it.description)
                view.error_container.visibility = View.VISIBLE
                view.progress_bar.visibility = View.GONE
            } else {
                view.error_container.visibility = View.GONE
            }
        })

        if (categoriesViewModel.getPwasCategories().value!!.isEmpty()) {
            categoriesViewModel.loadCategories(requireContext())
        }
        return view
    }

}
