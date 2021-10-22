package foundation.e.apps.categories.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import foundation.e.apps.databinding.CategoriesListItemBinding
import javax.inject.Singleton

@Singleton
class CategoriesRVAdapter : RecyclerView.Adapter<CategoriesRVAdapter.ViewHolder>() {

    private var oldList = mapOf<String, Int>()

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
            categoryIcon.load(oldList.values.toList()[position])
            categoryTitle.text = oldList.keys.toList()[position]
        }
    }

    override fun getItemCount(): Int {
        return oldList.size
    }

    fun setData(newList: Map<String, Int>) {
        val diffUtil = CategoriesDiffUtil(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldList = newList
        diffResult.dispatchUpdatesTo(this)
    }
}
