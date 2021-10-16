package foundation.e.apps.api.cleanapk.data.home

import foundation.e.apps.api.data.HomeApp

data class Home(
    val top_updated_apps: List<HomeApp>,
    val top_updated_games: List<HomeApp>,
    val banner_apps: List<HomeApp>,
    val popular_apps_in_last_24_hours: List<HomeApp>,
    val popular_games_in_last_24_hours: List<HomeApp>,
    val discover: List<HomeApp>
)
