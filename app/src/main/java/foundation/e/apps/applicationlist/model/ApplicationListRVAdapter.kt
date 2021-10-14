package foundation.e.apps.applicationlist.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.cleanapk.data.search.CleanAPKSearchApp
import foundation.e.apps.api.data.Origin
import foundation.e.apps.databinding.ApplicationListItemBinding
import javax.inject.Singleton

@Singleton
class ApplicationListRVAdapter :
    RecyclerView.Adapter<ApplicationListRVAdapter.ViewHolder>() {

    private var oldList = emptyList<CleanAPKSearchApp>()

    inner class ViewHolder(val binding: ApplicationListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ApplicationListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            appAuthor.text = oldList[position].author
            appTitle.text = oldList[position].name
            if (oldList[position].origin == Origin.CLEANAPK) {
                appIcon.load(CleanAPKInterface.ASSET_URL + oldList[position].icon_image_path)
            } else if (oldList[position].origin == Origin.GPLAY) {
                appIcon.load(oldList[position].icon_image_path)
            }
        }
    }

    override fun getItemCount(): Int {
        return oldList.size
    }

    fun setData(newList: List<CleanAPKSearchApp>) {
        val diffUtil = ApplicationListDiffUtil(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldList = newList
        diffResult.dispatchUpdatesTo(this)
    }
}