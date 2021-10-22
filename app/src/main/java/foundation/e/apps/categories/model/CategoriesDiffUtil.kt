package foundation.e.apps.categories.model

import androidx.recyclerview.widget.DiffUtil

class CategoriesDiffUtil(
    private val oldList: Map<String, Int>,
    private val newList: Map<String, Int>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList.keys.toList()[oldItemPosition] == newList.keys.toList()[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList.keys.toList()[oldItemPosition] != newList.keys.toList()[newItemPosition] -> false
            oldList.values.toList()[oldItemPosition] != newList.values.toList()[newItemPosition] -> false
            else -> true
        }
    }
}