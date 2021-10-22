package foundation.e.apps.categories.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import foundation.e.apps.api.fused.data.CategoryApp
import foundation.e.apps.categories.CategoriesFragmentDirections
import foundation.e.apps.databinding.CategoriesListItemBinding

class CategoriesRVAdapter :
    RecyclerView.Adapter<CategoriesRVAdapter.ViewHolder>() {

    private var oldList = listOf<CategoryApp>()

    inner class ViewHolder(val binding: CategoriesListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CategoriesListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            categoryLayout.setOnClickListener {
                val direction =
                    CategoriesFragmentDirections.actionCategoriesFragmentToApplicationListFragment(
                        oldList[position].id,
                        oldList[position].name
                    )
                holder.itemView.findNavController().navigate(direction)
            }
            categoryIcon.load(oldList[position].drawable)
            categoryTitle.text = oldList[position].name
        }
    }

    override fun getItemCount(): Int {
        return oldList.size
    }

    fun setData(newList: List<CategoryApp>) {
        val diffUtil = CategoriesDiffUtil(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldList = newList
        diffResult.dispatchUpdatesTo(this)
    }
}
