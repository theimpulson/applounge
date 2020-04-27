/*
    Copyright (C) 2019  e Foundation

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.application

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import foundation.e.apps.R
import foundation.e.apps.application.model.Application
import foundation.e.apps.application.model.ApplicationStateListener
import foundation.e.apps.application.model.Downloader
import foundation.e.apps.application.model.State
import foundation.e.apps.application.model.data.BasicData
import foundation.e.apps.application.model.data.PwasBasicData
import foundation.e.apps.application.viewmodel.ApplicationViewModel
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Common.toMiB
import foundation.e.apps.utils.Error
import foundation.e.apps.utils.Execute
import kotlinx.android.synthetic.main.application_list_item.view.*
import kotlinx.android.synthetic.main.simple_install_button_layout.view.*


class SmallApplicationViewHolder(private val activity: Activity, private val view: View) :
        RecyclerView.ViewHolder(view),
        ApplicationStateListener,
        Downloader.DownloadProgressCallback,
        BasicData.IconLoaderCallback,
        PwasBasicData.IconLoaderCallback{

    private val icon: ImageView = view.app_icon
    private val title: TextView = view.app_title
    private val installButton: Button = view.app_install
    private var application: Application? = null

    private val applicationViewModel = ApplicationViewModel()
    private var downloader: Downloader? = null

    init {
        view.setOnClickListener {
            if (application != null) {
                applicationViewModel.onApplicationClick(view.context, application!!)
            }
        }
        installButton.setOnClickListener {
            if (application?.fullData != null &&
                    application!!.fullData!!.getLastVersion() == null) {
                Snackbar.make(view, activity.getString(
                        Error.APK_UNAVAILABLE.description),
                        Snackbar.LENGTH_LONG).show()
            }
            else if(application?.pwabasicdata!=null){
                application?.pwaInstall(activity)
            }
            else{
                application?.buttonClicked(activity, activity)
            }
        }
    }

    fun createApplicationView(app: Application) {
        if(app.basicData!=null) {
            this.application?.removeListener(this)
            this.application = app
            icon.setImageDrawable(view.context.resources.getDrawable(R.drawable.ic_app_default))
            application!!.loadIcon(this)
            application!!.addListener(this)
            title.text = application!!.basicData!!.name
        }else if(app.pwabasicdata!=null){
            this.application?.removeListener(this)
            this.application = app
            icon.setImageDrawable(view.context.resources.getDrawable(R.drawable.ic_app_default))
            application!!.PwaloadIcon(this)
            application!!.addListener(this)
            title.text = application!!.pwabasicdata!!.name
        }

        stateChanged(application!!.state)
    }

    override fun onIconLoaded(application: Application, bitmap: Bitmap) {
        if (this.application != null && application == this.application) {
            icon.setImageBitmap(bitmap)
        }
    }

    override fun stateChanged(state: State) {
        Execute({}, {
            installButton.setTextColor(Color.parseColor("#0088ED"))
            installButton.setBackgroundResource(R.drawable.app_install_border_simple)
            installButton.text = activity.getString(state.installButtonTextId)
            installButton.clearAnimation()
            installButton.clearFocus();
            when (state) {

                State.NOT_DOWNLOADED ->{
                    installButton.setTextColor(Color.parseColor("#0088ED"))
                    installButton.setBackgroundResource(R.drawable.app_install_border_simple)
                    installButton.isEnabled = true
                }

                State.INSTALLED -> {
                    installButton.isEnabled =
                            Common.appHasLaunchActivity(activity, application!!.packageName)
                    installButton.setTextColor(Color.parseColor("#FAFAFA"))
                    installButton!!.setBackgroundResource(R.drawable.app_install_border)
                }
                State.INSTALLING -> {
                    installButton.isEnabled = false
                    installButton.setBackgroundResource(R.drawable.app_install_border_simple)
                    installingAnimation()
                }
                State.NOT_UPDATED -> {
                    installButton.setTextColor(Color.parseColor("#FAFAFA"))
                    installButton!!.setBackgroundResource(R.drawable.app_install_border)
                    installButton.isEnabled = true
                }
                else -> {
                    installButton.isEnabled = true
                }
            }
        })
    }

    override fun downloading(downloader: Downloader) {
        this.downloader = downloader
        this.downloader!!.addListener(this)
    }

    @SuppressLint("SetTextI18n")
    override fun notifyDownloadProgress(count: Int, total: Int) {
        installButton.setTextColor(Color.parseColor("#0088ED"))
        installButton.setGravity(Gravity.CENTER)
        installButton.setBackgroundResource(R.drawable.app_installing_border_simple)
        installButton.text = ((toMiB(count) / toMiB(total)) * 100).toInt().toString() + "%"
    }

    private fun installingAnimation() {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 200 //You can manage the blinking time with this parameter
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        installButton.startAnimation(anim)
    }

    override fun anErrorHasOccurred(error: Error) {
        Snackbar.make(activity.findViewById(R.id.container),
                activity.getString(error.description),
                Snackbar.LENGTH_LONG).show()

    }

    fun onViewRecycled() {
        downloader?.removeListener(this)
        downloader = null
    }
}
