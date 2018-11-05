package io.eelo.appinstaller.home.model

import android.content.Context
import android.os.AsyncTask
import io.eelo.appinstaller.utils.ImagesLoader

class BannerAppsLoader(private val homeModel: HomeModel, private val homeResult: HomeResult) : AsyncTask<Context, Any, List<BannerApp>>() {

    override fun doInBackground(vararg params: Context): List<BannerApp> {
        return loadBannerImages(params[0])
    }

    override fun onPostExecute(result: List<BannerApp>) {
        homeModel.bannerApps.value = result
    }

    private fun loadBannerImages(context: Context): ArrayList<BannerApp> {
        val apps = homeResult.bannerApps(homeModel.installManager, context)
        val imagesUris = ArrayList<String>()
        apps.forEach {
            imagesUris.add(it.data.images[0])
        }
        val images = ImagesLoader(imagesUris).loadImages()
        val bannerApps = ArrayList<BannerApp>()
        apps.forEachIndexed { index, application ->
            bannerApps.add(BannerApp(application, images[index]!!))
        }
        return bannerApps
    }

}