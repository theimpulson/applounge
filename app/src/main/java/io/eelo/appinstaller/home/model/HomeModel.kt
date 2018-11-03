package io.eelo.appinstaller.home.model

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.ImageDownloader
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utlis.Constants
import java.net.URL
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import kotlin.collections.HashMap

class HomeModel : HomeModelInterface, AsyncTask<Any, Any, Any>() {

    private lateinit var onLoad: () -> Unit
    private lateinit var context: Context
    private lateinit var installManager: InstallManager

    override fun doInBackground(vararg params: Any?): Any? {
        val result = loadResult()
        loadCarouselImages(result)
        loadApplications(result)

        return null
    }

    override fun onPostExecute(result: Any?) {
        onLoad.invoke()
    }


    override fun load(onLoad: () -> Unit, context: Context, installManager: InstallManager) {
        this.onLoad = onLoad
        this.context = context
        this.installManager = installManager

        execute(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    val carouselImages = ArrayList<Pair<Application, Bitmap>>()
    val applications = HashMap<String, ArrayList<Application>>()

    companion object {
        private val homeResultReader = ObjectMapper().readerFor(HomeResult::class.java)
    }

    private fun loadResult(): HomeResult {
        val url = URL(Constants.BASE_URL + "apps?action=list_home")
        return homeResultReader.readValue<HomeResult>(url)
    }

    private fun loadCarouselImages(result: HomeResult) {
        val bannerApps = result.bannerApps(installManager, context)
        val queue = LinkedBlockingQueue<Pair<Application, Bitmap>>()

        bannerApps.forEach {
            loadImage(queue, it)
        }

        for (i in 1..bannerApps.size) {
            val carouselImage = queue.take()
            carouselImages.add(carouselImage)
        }
    }

    private fun loadImage(queue: LinkedBlockingQueue<Pair<Application, Bitmap>>, application: Application) {
        ImageDownloader {
            val result = Pair(application, it)
            queue.put(result)
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, application.data.images[0])
    }

    private fun loadApplications(result: HomeResult) {
        applications["discover"] = result.discover(installManager, context)
        applications["top_updated_apps"] = result.topUpdatedApps(installManager, context)
        applications["top_updated_games"] = result.topUpdatedGames(installManager, context)
        applications["popular_apps_in_last_24h"] = result.popularAppsInLast24h(installManager, context)
        applications["popular_games_in_last_24h"] = result.popularGamesInLast24h(installManager, context)
        applications["popular_games_in_last_24h"] = result.popularGamesInLast24h(installManager, context)
    }
}
