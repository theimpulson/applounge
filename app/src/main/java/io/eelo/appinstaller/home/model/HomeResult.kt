package io.eelo.appinstaller.home.model

import android.content.Context
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.ApplicationData
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utils.ApplicationParser.Companion.parseToApps

class HomeResult @JsonCreator
constructor(@JsonProperty("success") val success: Boolean,
            @JsonProperty("home") private val child: SubHomeResult) {

    fun topUpdatedApps(installManager: InstallManager, context: Context): ArrayList<Application> {
        return parseToApps(installManager, context, child.topUpdatedApps)
    }

    fun topUpdatedGames(installManager: InstallManager, context: Context): ArrayList<Application> {
        return parseToApps(installManager, context, child.topUpdatedGames)
    }

    fun bannerApps(installManager: InstallManager, context: Context): ArrayList<Application> {
        return parseToApps(installManager, context, child.bannerApps)
    }

    fun popularAppsInLast24h(installManager: InstallManager, context: Context): ArrayList<Application> {
        return parseToApps(installManager, context, child.popularAppsInLast24h)
    }

    fun popularGamesInLast24h(installManager: InstallManager, context: Context): ArrayList<Application> {
        return parseToApps(installManager, context, child.popularGamesInLast24h)
    }

    fun discover(installManager: InstallManager, context: Context): ArrayList<Application> {
        return parseToApps(installManager, context, child.discover)
    }

    class SubHomeResult @JsonCreator
    constructor(@JsonProperty("top_updated_apps") val topUpdatedApps: Array<ApplicationData>,
                @JsonProperty("top_updated_games") val topUpdatedGames: Array<ApplicationData>,
                @JsonProperty("banner_apps") val bannerApps: Array<ApplicationData>,
                @JsonProperty("popular_apps_in_last_24_hours") val popularAppsInLast24h: Array<ApplicationData>,
                @JsonProperty("popular_games_in_last_24_hours") val popularGamesInLast24h: Array<ApplicationData>,
                @JsonProperty("discover") val discover: Array<ApplicationData>)


}