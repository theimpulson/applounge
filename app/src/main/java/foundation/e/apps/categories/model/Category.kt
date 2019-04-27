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

package foundation.e.apps.categories.model

import foundation.e.apps.R
import java.io.Serializable

class Category(val id: String) : Serializable {
    private val title: String
    private val iconResource: Int

    init {
        title = getCategoryTitle(id)
        iconResource = getCategoryIconResource(id)
    }

    private fun getCategoryTitle(categoryId: String): String {
        val title = categoryId.replace("_", " ")
        if (title.contains("game ")) {
            return title.removePrefix("game ").capitalize()
        }
        return title.capitalize()
    }

    private fun getCategoryIconResource(categoryId: String): Int {
        return when (categoryId) {
            "comics" ->
                R.drawable.ic_category_comics
            "education" ->
                R.drawable.ic_category_education
            "music_and_audio" ->
                R.drawable.ic_category_music_and_audio
            "entertainment" ->
                R.drawable.ic_category_entertainment
            "tools" ->
                R.drawable.ic_category_tools
            "communication" ->
                R.drawable.ic_category_communication
            "medical" ->
                R.drawable.ic_category_medical
            "lifestyle" ->
                R.drawable.ic_category_lifestyle
            "video_players" ->
                R.drawable.ic_category_video_players
            "events" ->
                R.drawable.ic_category_events
            "productivity" ->
                R.drawable.ic_category_productivity
            "house_and_home" ->
                R.drawable.ic_category_house_and_home
            "art_and_design" ->
                R.drawable.ic_category_art_and_design
            "photography" ->
                R.drawable.ic_category_photography
            "auto_and_vehicles" ->
                R.drawable.ic_category_auto_and_vehicles
            "books_and_reference" ->
                R.drawable.ic_category_books_and_reference
            "social" ->
                R.drawable.ic_category_social
            "travel_and_local" ->
                R.drawable.ic_category_travel_and_local
            "beauty" ->
                R.drawable.ic_cateogry_beauty
            "personalization" ->
                R.drawable.ic_category_personalization
            "business" ->
                R.drawable.ic_category_business
            "health_and_fitness" ->
                R.drawable.ic_category_health_and_fitness
            "dating" ->
                R.drawable.ic_category_dating
            "news_and_magazines" ->
                R.drawable.ic_category_news_and_magazines
            "finance" ->
                R.drawable.ic_category_finance
            "food_and_drink" ->
                R.drawable.ic_category_food_and_drink
            "shopping" ->
                R.drawable.ic_category_shopping
            "libraries_and_demo" ->
                R.drawable.ic_category_libraries_and_demo
            "sports" ->
                R.drawable.ic_category_sports
            "maps_and_navigation" ->
                R.drawable.ic_category_maps_and_navigation
            "parenting" ->
                R.drawable.ic_category_parenting
            "weather" ->
                R.drawable.ic_category_weather
            "topic/family" ->
                R.drawable.ic_category_family
            "game_card" ->
                R.drawable.ic_category_game_card
            "game_action" ->
                R.drawable.ic_category_game_action
            "game_board" ->
                R.drawable.ic_category_game_board
            "game_role_playing" ->
                R.drawable.ic_category_game_role_playing
            "game_arcade" ->
                R.drawable.ic_category_game_arcade
            "game_casino" ->
                R.drawable.ic_category_game_casino
            "game_adventure" ->
                R.drawable.ic_category_game_adventure
            "game_casual" ->
                R.drawable.ic_category_game_casual
            "game_puzzle" ->
                R.drawable.ic_category_game_puzzle
            "game_strategy" ->
                R.drawable.ic_category_game_strategy
            "game_educational" ->
                R.drawable.ic_category_game_educational
            "game_music" ->
                R.drawable.ic_category_game_music
            "game_racing" ->
                R.drawable.ic_category_game_racing
            "game_simulation" ->
                R.drawable.ic_category_game_simulation
            "game_sports" ->
                R.drawable.ic_category_game_sports
            "game_trivia" ->
                R.drawable.ic_category_game_trivia
            "game_word" ->
                R.drawable.ic_category_game_word
            else ->
                R.drawable.ic_category_default
        }
    }

    fun getTitle(): String {
        return title
    }

    fun getIconResource(): Int {
        return iconResource
    }
}
