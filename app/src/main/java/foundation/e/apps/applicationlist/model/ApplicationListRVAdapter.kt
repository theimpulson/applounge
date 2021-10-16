package foundation.e.apps.applicationlist.model

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.data.Origin
import foundation.e.apps.api.data.SearchApp
import foundation.e.apps.databinding.ApplicationListItemBinding
import javax.inject.Singleton

@Singleton
class ApplicationListRVAdapter :
    RecyclerView.Adapter<ApplicationListRVAdapter.ViewHolder>() {

    private var oldList = emptyList<SearchApp>()
    private val TAG = ApplicationListRVAdapter::class.java.simpleName

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
            appTitle.text = oldList[position].name
            appAuthor.text = oldList[position].author
            if (oldList[position].ratings.usageQualityScore != -1.0) {
                appRating.text = oldList[position].ratings.usageQualityScore.toString()
                appRatingBar.rating = oldList[position].ratings.usageQualityScore.toFloat()
            }
            if (oldList[position].ratings.privacyScore != -1.0) {
                appPrivacyScore.text = oldList[position].ratings.privacyScore.toString()
            }
            when (oldList[position].origin) {
                Origin.GPLAY -> {
                    appIcon.load(oldList[position].icon_image_path)
                }
                Origin.CLEANAPK -> {
                    appIcon.load(CleanAPKInterface.ASSET_URL + oldList[position].icon_image_path)
                }
                else -> Log.wtf(TAG, "${oldList[position].package_name} is from an unknown origin")
            }
        }
    }

    override fun getItemCount(): Int {
        return oldList.size
    }

    fun setData(newList: List<SearchApp>) {
        val diffUtil = ApplicationListDiffUtil(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldList = newList
        diffResult.dispatchUpdatesTo(this)
    }
}
