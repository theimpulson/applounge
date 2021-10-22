package foundation.e.apps.applicationlist.model

import androidx.recyclerview.widget.DiffUtil
import foundation.e.apps.api.fused.data.SearchApp

class ApplicationListDiffUtil(
    private val oldList: List<SearchApp>,
    private val newList: List<SearchApp>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Check both id and package name as we are fetching data from multiple sources to avoid issues
        return oldList[oldItemPosition]._id == newList[newItemPosition]._id && oldList[oldItemPosition].package_name == newList[newItemPosition].package_name
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList[oldItemPosition]._id != newList[newItemPosition]._id -> false
            oldList[oldItemPosition].name != newList[newItemPosition].name -> false
            oldList[oldItemPosition].author != newList[newItemPosition].author -> false
            oldList[oldItemPosition].ratings.privacyScore != newList[newItemPosition].ratings.privacyScore -> false
            oldList[oldItemPosition].ratings.usageQualityScore != newList[newItemPosition].ratings.usageQualityScore -> false
            oldList[oldItemPosition].icon_image_path != newList[newItemPosition].icon_image_path -> false
            else -> true
        }
    }
}
