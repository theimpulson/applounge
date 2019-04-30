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

package foundation.e.apps.home.model

import android.os.AsyncTask
import foundation.e.apps.application.model.Application
import foundation.e.apps.utils.ImagesLoader

class BannerApplicationLoader(private val apps: ArrayList<Application>, private val homeModel: HomeModel) : AsyncTask<Any, Any, ArrayList<BannerApplication>>() {

    override fun doInBackground(vararg params: Any): ArrayList<BannerApplication> {
        return loadBannerImages()
    }

    override fun onPostExecute(result: ArrayList<BannerApplication>) {
        homeModel.bannerApplications.value = result
    }

    private fun loadBannerImages(): ArrayList<BannerApplication> {
        val bannerApplications = ArrayList<BannerApplication>()
        apps.forEach { application ->
            if (application.basicData!!.imagesUri.isNotEmpty()) {
                val image = Array(1) { application.basicData!!.imagesUri[0] }
                ImagesLoader(image).loadImages().forEach {
                    bannerApplications.add(BannerApplication(application, it))
                }
            } else {
                application.decrementUses()
            }
        }
        return bannerApplications
    }

}
