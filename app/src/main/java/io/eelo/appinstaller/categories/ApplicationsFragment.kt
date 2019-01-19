package io.eelo.appinstaller.categories

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.eelo.appinstaller.R
import io.eelo.appinstaller.categories.viewmodel.CategoriesViewModel
import io.eelo.appinstaller.utils.Common
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_application_categories.view.*

class ApplicationsFragment : Fragment() {
    private lateinit var categoriesViewModel: CategoriesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        categoriesViewModel = ViewModelProviders.of(activity!!).get(CategoriesViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_application_categories, container, false)
        view.categories_list.layoutManager = LinearLayoutManager(context)

        view.categories_list.visibility = View.GONE
        view.progress_bar.visibility = View.VISIBLE
        view.error_container.visibility = View.GONE
        view.findViewById<TextView>(R.id.error_resolve).setOnClickListener {
            view.progress_bar.visibility = View.VISIBLE
            categoriesViewModel.loadCategories(context!!)
        }

        // Bind to the list of applications categories
        categoriesViewModel.getApplicationsCategories().observe(this, Observer {
            if (it!!.isNotEmpty()) {
                view.categories_list.adapter = CategoriesListAdapter(it)
                view.categories_list.visibility = View.VISIBLE
                view.progress_bar.visibility = View.GONE
            }
        })

        // Bind to the screen error
        categoriesViewModel.getScreenError().observe(this, Observer {
            if (it != null) {
                view.error_description.text = activity!!.getString(it.description)
                view.error_container.visibility = View.VISIBLE
                view.progress_bar.visibility = View.GONE
            } else {
                view.error_container.visibility = View.GONE
            }
        })

        if (categoriesViewModel.getApplicationsCategories().value!!.isEmpty()) {
            categoriesViewModel.loadCategories(context!!)
        }
        return view
    }
}
