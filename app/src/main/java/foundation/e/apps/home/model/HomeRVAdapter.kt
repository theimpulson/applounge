package foundation.e.apps.home.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.data.HomeApp
import foundation.e.apps.databinding.HomeListItemBinding

class HomeRVAdapter : RecyclerView.Adapter<HomeRVAdapter.ViewHolder>() {

    private var oldList = emptyList<HomeApp>()

    inner class ViewHolder(val binding: HomeListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = HomeListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val params = view.root.layoutParams
        params.width = (parent.width * 0.3).toInt()
        view.root.layoutParams = params
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            appIcon.load(CleanAPKInterface.ASSET_URL + oldList[position].icon_image_path)
            appName.text = oldList[position].name
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