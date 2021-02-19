/*
    Copyright (C) 2019  e Foundation

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.categories

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import foundation.e.apps.R
import foundation.e.apps.categories.viewmodel.CategoriesViewModel
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_games_categories.view.*

class GamesFragment() : Fragment() {
    private lateinit var categoriesViewModel: CategoriesViewModel

    var color:Int = 0;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        categoriesViewModel = ViewModelProviders.of(activity!!).get(CategoriesViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_games_categories, container, false)
        view.categories_list.layoutManager = LinearLayoutManager(context)
        color = arguments!!.getInt("color",0)
        view.categories_list.visibility = View.GONE
        view.progress_bar.visibility = View.VISIBLE
        view.error_container.visibility = View.GONE
        view.findViewById<TextView>(R.id.error_resolve).setOnClickListener {
            view.progress_bar.visibility = View.VISIBLE
            categoriesViewModel.loadCategories(context!!)
        }
        view.error_resolve.setTextColor(Color.parseColor("#ffffff"))
        if (color != null) {
            view.error_resolve.setBackgroundColor(color!!)
        }


        // Bind to the list of games categories
        categoriesViewModel.getGamesCategories().observe(this, Observer {
            if (it!!.isNotEmpty()) {
                view.categories_list.adapter = CategoriesListAdapter(it, color)
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

        if (categoriesViewModel.getGamesCategories().value!!.isEmpty()) {
            categoriesViewModel.loadCategories(context!!)
        }
        return view
    }

    companion object{
        fun newInstance(color:Int?) : GamesFragment{
            val gamesFragment = GamesFragment()
            val bundle = Bundle()
            bundle.putInt("color",color!!)
            gamesFragment.arguments = bundle
            return  gamesFragment
        }
    }
}
