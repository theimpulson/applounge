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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import foundation.e.apps.categories.category.CategoryActivity
import foundation.e.apps.categories.model.Category
import foundation.e.apps.databinding.CategoryListItemBinding
import foundation.e.apps.utils.Constants

class CategoriesListAdapter(private val context: Context, private var categories: ArrayList<Category>, color: Int?)
    : RecyclerView.Adapter<CategoriesListAdapter.CategoryViewHolder>() {

    val color = color;
    init {
        categories = ArrayList(categories.sortedWith(
                compareBy({ it.getTitle() }, { it.getTitle() })))

    }

    class CategoryViewHolder(binding: CategoryListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val categoryContainer = binding.categoryContainer
        val categoryIcon = binding.categoryIcon
        val categoryTitle = binding.categoryTitle
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            CategoryListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {


        holder.categoryIcon.setImageDrawable(AppCompatResources.getDrawable(context, categories[position].getIconResource()))
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
