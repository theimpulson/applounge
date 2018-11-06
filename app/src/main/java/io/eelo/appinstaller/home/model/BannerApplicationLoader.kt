package io.eelo.appinstaller.home.model

import android.content.Context
import android.os.AsyncTask
import io.eelo.appinstaller.utils.ImagesLoader

class BannerApplicationLoader(private val homeModel: HomeModel, private val homeResult: HomeResult) : AsyncTask<Context, Any, ArrayList<BannerApplication>>() {

    override fun doInBackground(vararg params: Context): ArrayList<BannerApplication> {
        return loadBannerImages(params[0])
    }

    override fun onPostExecute(result: ArrayList<BannerApplication>) {
        homeModel.bannerApplications.value = result
    }

    private fun loadBannerImages(context: Context): ArrayList<BannerApplication> {
        val apps = homeResult.bannerApps(homeModel.getInstallManager(), context)
        val imagesUris = ArrayList<String>()
        apps.forEach {
            imagesUris.add(it.data.images[0])
        }
        val images = ImagesLoader(imagesUris).loadImages()
        val bannerApps = ArrayList<BannerApplication>()
        apps.forEachIndexed { index, application ->
            bannerApps.add(BannerApplication(application, images[index]!!))
        }
        return bannerApps
    }

}
