package foundation.e.apps.applicationlist.model

import androidx.recyclerview.widget.DiffUtil
import foundation.e.apps.api.cleanapk.data.search.App

class ApplicationListDiffUtil(
    private val oldList: List<App>,
    private val newList: List<App>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition]._id == newList[newItemPosition]._id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList[oldItemPosition]._id != newList[newItemPosition]._id -> false
            oldList[oldItemPosition].name != newList[newItemPosition].name -> false
            oldList[oldItemPosition].author != newList[newItemPosition].author -> false
            oldList[oldItemPosition].icon_image_path != newList[newItemPosition].icon_image_path -> false
            else -> true
        }
    }
}