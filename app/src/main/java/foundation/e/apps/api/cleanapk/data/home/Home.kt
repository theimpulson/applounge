package foundation.e.apps.api.cleanapk.data.home

data class Home(
    val top_updated_apps: List<Application>,
    val top_updated_games: List<Application>,
    val banner_apps: List<Application>,
    val popular_apps_in_last_24_hours: List<Application>,
    val popular_games_in_last_24_hours: List<Application>,
    val discover: List<Application>
)