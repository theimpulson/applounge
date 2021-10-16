package foundation.e.apps.home.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.load
import coil.size.Scale
import com.google.android.material.progressindicator.CircularProgressIndicator
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.data.HomeApp
import foundation.e.apps.databinding.HomeFeaturedListItemBinding
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeVPAdapter: RecyclerView.Adapter<HomeVPAdapter.ViewHolder>() {

    private var oldList = emptyList<HomeApp>()

    inner class ViewHolder(val binding: HomeFeaturedListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HomeFeaturedListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            imageView.load(CleanAPKInterface.ASSET_URL + oldList[position].other_images_path[0])
        }
    }

    override fun getItemCount(): Int {
        return oldList.size
    }

    fun setData(newList: List<HomeApp>) {
        val diffUtil = HomeDiffUtil(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldList = newList
        diffResult.dispatchUpdatesTo(this)
    }
}