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

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import foundation.e.apps.R
import foundation.e.apps.categories.category.CategoryActivity
import foundation.e.apps.categories.model.Category
import foundation.e.apps.utils.Constants

class CategoriesListAdapter(private var categories: ArrayList<Category>, color: Int?)
    : RecyclerView.Adapter<CategoriesListAdapter.CategoryViewHolder>() {

    val color = color;
    init {
        categories = ArrayList(categories.sortedWith(
                compareBy({ it.getTitle() }, { it.getTitle() })))

    }

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryContainer: RelativeLayout = view.findViewById(R.id.category_container)
        val categoryIcon: ImageView = view.findViewById(R.id.category_icon)
        val categoryTitle: TextView = view.findViewById(R.id.category_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val categoryContainer = LayoutInflater.from(parent.context).inflate(
                R.layout.category_list_item,
                parent, false)
        return CategoryViewHolder(categoryContainer)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {


        holder.categoryIcon.setImageDrawable(holder.categoryIcon.resources.getDrawable(categories[position].getIconResource()))
        //holder.categoryIcon.setColorFilter(Color.parseColor("#0088ED"))
        if (color != null) {
            holder.categoryIcon.setColorFilter(color)
        }
        holder.categoryTitle.text = categories[position].getTitle()
        holder.categoryContainer.setOnClickListener {
            val intent = Intent(holder.categoryContainer.context, CategoryActivity::class.java)
            intent.putExtra(Constants.CATEGORY_KEY, categories[position])
            intent.putExtra("POSITION", position)
            holder.categoryContainer.context.startActivity(intent)
        }
    }
}
