package foundation.e.apps.categories

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import foundation.e.apps.R
import foundation.e.apps.categories.category.CategoryActivity
import foundation.e.apps.categories.model.Category
import foundation.e.apps.utils.Constants
import kotlin.collections.ArrayList

class CategoriesListAdapter(private var categories: ArrayList<Category>)
    : RecyclerView.Adapter<CategoriesListAdapter.CategoryViewHolder>() {

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

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.categoryIcon.setImageDrawable(
                holder.categoryIcon.resources.getDrawable(categories[position].getIconResource()))
        holder.categoryTitle.text = categories[position].getTitle()
        holder.categoryContainer.setOnClickListener {
            val intent = Intent(holder.categoryContainer.context, CategoryActivity::class.java)
            intent.putExtra(Constants.CATEGORY_KEY, categories[position])
            holder.categoryContainer.context.startActivity(intent)
        }
    }
}
