package io.eelo.appinstaller.home.model

import android.os.AsyncTask
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.utils.ImagesLoader

class BannerApplicationLoader(private val apps: ArrayList<Application>, private val homeModel: HomeModel) : AsyncTask<Any, Any, ArrayList<BannerApplication>>() {

    override fun doInBackground(vararg params: Any): ArrayList<BannerApplication> {
        return loadBannerImages()
    }

    override fun onPostExecute(result: ArrayList<BannerApplication>) {
        homeModel.bannerApplications.value = result
    }

    private fun loadBannerImages(): ArrayList<BannerApplication> {
        val imagesUris = ArrayList<String>()
        apps.forEach {
            if (it.basicData!!.imagesUri.isNotEmpty()) {
                imagesUris.add(it.basicData!!.imagesUri[0])
            } else {
                it.decrementUses()
            }
        }
        val images = ImagesLoader(imagesUris.toTypedArray()).loadImages()
        val bannerApps = ArrayList<BannerApplication>()
        imagesUris.forEachIndexed { index, uri ->
            bannerApps.add(BannerApplication(apps[index], images[index]))
        }
        return bannerApps
    }

}
