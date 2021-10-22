package foundation.e.apps.applicationlist.model

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.load
import foundation.e.apps.R
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.api.fused.data.SearchApp
import foundation.e.apps.databinding.ApplicationListItemBinding
import foundation.e.apps.search.SearchFragmentDirections
import javax.inject.Singleton

@Singleton
class ApplicationListRVAdapter(private val fusedAPIInterface: FusedAPIInterface) :
    RecyclerView.Adapter<ApplicationListRVAdapter.ViewHolder>() {

    private var oldList = emptyList<SearchApp>()
    private val TAG = ApplicationListRVAdapter::class.java.simpleName

    lateinit var circularProgressDrawable: CircularProgressDrawable

    inner class ViewHolder(val binding: ApplicationListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Setup progress drawable for coil placeholder
        circularProgressDrawable = CircularProgressDrawable(parent.context)
        circularProgressDrawable.strokeWidth = 10f
        circularProgressDrawable.centerRadius = 50f
        circularProgressDrawable.colorFilter = PorterDuffColorFilter(
            parent.context.getColor(R.color.colorAccent),
            PorterDuff.Mode.SRC_IN
        )

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
            applicationList.setOnClickListener {
                val action = oldList[position].origin?.let { origin ->
                    SearchFragmentDirections.actionSearchFragmentToApplicationFragment(
                        oldList[position]._id,
                        oldList[position].package_name,
                        origin
                    )
                }
                action?.let { direction -> holder.itemView.findNavController().navigate(direction) }
            }
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
                    appIcon.load(oldList[position].icon_image_path) {
                        placeholder(circularProgressDrawable)
                    }
                }
                Origin.CLEANAPK -> {
                    appIcon.load(CleanAPKInterface.ASSET_URL + oldList[position].icon_image_path) {
                        placeholder(circularProgressDrawable)
                    }
                }
                else -> Log.wtf(TAG, "${oldList[position].package_name} is from an unknown origin")
            }
            installButton.setOnClickListener {
                fusedAPIInterface.getApplication(
                    oldList[position]._id,
                    oldList[position].name,
                    oldList[position].package_name,
                    oldList[position].latest_version_code,
                    oldList[position].offerType,
                    oldList[position].origin
                )
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
