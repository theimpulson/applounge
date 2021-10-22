package foundation.e.apps.categories.model

import androidx.recyclerview.widget.DiffUtil
import foundation.e.apps.api.fused.data.CategoryApp

class CategoriesDiffUtil(
    private val oldList: List<CategoryApp>,
    private val newList: List<CategoryApp>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList[oldItemPosition].id != newList[newItemPosition].id -> false
            oldList[oldItemPosition].name != newList[newItemPosition].name -> false
            oldList[oldItemPosition].drawable != newList[newItemPosition].drawable -> false
            else -> true
        }
    }
}
