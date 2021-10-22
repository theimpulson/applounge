package foundation.e.apps.home.model

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.load
import foundation.e.apps.R
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.HomeApp
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.databinding.HomeListItemBinding

class HomeRVAdapter(private val fusedAPIInterface: FusedAPIInterface) :
    RecyclerView.Adapter<HomeRVAdapter.ViewHolder>() {

    private var oldList = emptyList<HomeApp>()

    lateinit var circularProgressDrawable: CircularProgressDrawable

    inner class ViewHolder(val binding: HomeListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Setup progress drawable for coil placeholder
        circularProgressDrawable = CircularProgressDrawable(parent.context)
        circularProgressDrawable.strokeWidth = 10f
        circularProgressDrawable.centerRadius = 50f
        circularProgressDrawable.colorFilter = PorterDuffColorFilter(
            parent.context.getColor(R.color.colorAccent),
            PorterDuff.Mode.SRC_IN
        )

        val view = HomeListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val params = view.root.layoutParams
        params.width = (parent.width * 0.3).toInt()
        view.root.layoutParams = params
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            appIcon.load(CleanAPKInterface.ASSET_URL + oldList[position].icon_image_path) {
                placeholder(circularProgressDrawable)
            }
            appName.text = oldList[position].name
            installButton.setOnClickListener {
                // Send dummy values as we are fetching home screen data from cleanAPK
                fusedAPIInterface.getApplication(
                    oldList[position]._id,
                    oldList[position].name,
                    "",
                    0,
                    0,
                    Origin.CLEANAPK
                )
            }
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
