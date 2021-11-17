/*
 * Apps  Quickly and easily install Android apps onto your device!
 * Copyright (C) 2021  E FOUNDATION
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.applicationlist.model

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import foundation.e.apps.R
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.api.fused.data.Status
import foundation.e.apps.applicationlist.ApplicationListFragmentDirections
import foundation.e.apps.databinding.ApplicationListItemBinding
import foundation.e.apps.search.SearchFragmentDirections
import foundation.e.apps.utils.pkg.PkgManagerModule
import javax.inject.Singleton

@Singleton
class ApplicationListRVAdapter(
    private val fusedAPIInterface: FusedAPIInterface,
    private val currentDestinationId: Int,
    private val pkgManagerModule: PkgManagerModule
) :
    RecyclerView.Adapter<ApplicationListRVAdapter.ViewHolder>() {

    private var oldList = emptyList<FusedApp>()
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
        val view = holder.itemView
        val searchApp = oldList[position]
        holder.binding.apply {
            applicationList.setOnClickListener {
                val action = when (currentDestinationId) {
                    R.id.applicationListFragment -> {
                        ApplicationListFragmentDirections.actionApplicationListFragmentToApplicationFragment(
                            searchApp._id,
                            searchApp.package_name,
                            searchApp.origin
                        )
                    }
                    R.id.searchFragment -> {
                        SearchFragmentDirections.actionSearchFragmentToApplicationFragment(
                            searchApp._id,
                            searchApp.package_name,
                            searchApp.origin
                        )
                    }
                    else -> null
                }
                action?.let { direction -> view.findNavController().navigate(direction) }
            }
            appTitle.text = searchApp.name
            appAuthor.text = searchApp.author
            if (searchApp.ratings.usageQualityScore != -1.0) {
                appRating.text = searchApp.ratings.usageQualityScore.toString()
                appRatingBar.rating = searchApp.ratings.usageQualityScore.toFloat()
            }
            if (searchApp.ratings.privacyScore != -1.0) {
                appPrivacyScore.text = searchApp.ratings.privacyScore.toString()
            }
            when (searchApp.origin) {
                Origin.GPLAY -> {
                    appIcon.load(searchApp.icon_image_path)
                }
                Origin.CLEANAPK -> {
                    appIcon.load(CleanAPKInterface.ASSET_URL + searchApp.icon_image_path)
                }
                else -> Log.wtf(TAG, "${searchApp.package_name} is from an unknown origin")
            }
            when (searchApp.status) {
                Status.INSTALLED -> {
                    installButton.text = view.context.getString(R.string.open)
                    installButton.setOnClickListener {
                        view.context.startActivity(pkgManagerModule.getLaunchIntent(searchApp.package_name))
                    }
                }
                Status.UPDATABLE -> {
                    installButton.text = view.context.getString(R.string.update)
                    installButton.setOnClickListener {
                        installApplication(searchApp)
                    }
                }
                Status.UNAVAILABLE -> {
                    installButton.setOnClickListener {
                        installApplication(searchApp)
                    }
                }
                Status.DOWNLOADING -> {
                    installButton.text = view.context.getString(R.string.cancel)
                }
                Status.INSTALLING, Status.UNINSTALLING -> {
                    installButton.text = view.context.getString(R.string.cancel)
                    installButton.isEnabled = false
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return oldList.size
    }

    fun setData(newList: List<FusedApp>) {
        val diffUtil = ApplicationListDiffUtil(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    private fun installApplication(searchApp: FusedApp) {
        fusedAPIInterface.getApplication(
            searchApp._id,
            searchApp.name,
            searchApp.package_name,
            searchApp.latest_version_code,
            searchApp.offer_type,
            searchApp.origin
        )
    }
}
