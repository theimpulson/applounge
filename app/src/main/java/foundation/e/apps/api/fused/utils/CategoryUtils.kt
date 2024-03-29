/*
 * Apps  Quickly and easily install Android apps onto your device!
 * Copyright (C) 2021  E FOUNDATION
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.api.fused.utils

import foundation.e.apps.R

object CategoryUtils {
    fun provideAppsCategoryIconResource(categoryId: String): Int {
        return when (categoryId) {
            "comics" ->
                R.drawable.ic_cat_comics
            "connectivity" ->
                R.drawable.ic_cat_connectivity
            "development" ->
                R.drawable.ic_cat_development
            "education" ->
                R.drawable.ic_cat_education
            "graphics" ->
                R.drawable.ic_cat_graphics
            "internet" ->
                R.drawable.ic_cat_internet
            "music_and_audio" ->
                R.drawable.ic_cat_music_and_audio
            "entertainment" ->
                R.drawable.ic_cat_entertainment
            "tools" ->
                R.drawable.ic_cat_tools
            "security" ->
                R.drawable.ic_cat_security
            "system" ->
                R.drawable.ic_cat_system
            "system_apps" ->
                R.drawable.ic_cat_system
            "communication" ->
                R.drawable.ic_cat_communication
            "medical" ->
                R.drawable.ic_cat_medical
            "lifestyle" ->
                R.drawable.ic_cat_lifestyle
            "video_players" ->
                R.drawable.ic_cat_video_players
            "video_players_and_editors" ->
                R.drawable.ic_cat_video_players
            "events" ->
                R.drawable.ic_cat_events
            "productivity" ->
                R.drawable.ic_cat_productivity
            "house_and_home" ->
                R.drawable.ic_cat_house_and_home
            "art_and_design" ->
                R.drawable.ic_art_and_design
            "photography" ->
                R.drawable.ic_cat_photography
            "auto_and_vehicles" ->
                R.drawable.ic_auto_and_vehicles
            "books_and_reference" ->
                R.drawable.ic_books_and_reference
            "social" ->
                R.drawable.ic_cat_social
            "travel_and_local" ->
                R.drawable.ic_cat_travel_and_local
            "beauty" ->
                R.drawable.ic_beauty
            "personalization" ->
                R.drawable.ic_cat_personalization
            "business" ->
                R.drawable.ic_business
            "health_and_fitness" ->
                R.drawable.ic_cat_health_and_fitness
            "dating" ->
                R.drawable.ic_cat_dating
            "news_and_magazines" ->
                R.drawable.ic_cat_news_and_magazine
            "finance" ->
                R.drawable.ic_cat_finance
            "food_and_drink" ->
                R.drawable.ic_cat_food_and_drink
            "shopping" ->
                R.drawable.ic_cat_shopping
            "libraries_and_demo" ->
                R.drawable.ic_cat_libraries_and_demo
            "sports" ->
                R.drawable.ic_cat_sports
            "maps_and_navigation" ->
                R.drawable.ic_cat_maps_and_navigation
            "parenting" ->
                R.drawable.ic_cat_parenting
            "weather" ->
                R.drawable.ic_cat_weather
            "topic/family" ->
                R.drawable.ic_cat_family
            "game_card" ->
                R.drawable.ic_cat_game_card
            "game_action" ->
                R.drawable.ic_cat_game_action
            "game_board" ->
                R.drawable.ic_cat_game_board
            "game_role_playing" ->
                R.drawable.ic_cat_game_role_playing
            "game_arcade" ->
                R.drawable.ic_cat_game_arcade
            "game_casino" ->
                R.drawable.ic_cat_game_casino
            "game_adventure" ->
                R.drawable.ic_cat_game_adventure
            "game_casual" ->
                R.drawable.ic_cat_game_casual
            "game_puzzle" ->
                R.drawable.ic_cat_game_puzzle
            "game_strategy" ->
                R.drawable.ic_cat_game_strategy
            "game_educational" ->
                R.drawable.ic_cat_game_educational
            "game_music" ->
                R.drawable.ic_cat_game_music
            "game_racing" ->
                R.drawable.ic_cat_game_racing
            "game_simulation" ->
                R.drawable.ic_cat_game_simulation
            "game_sports" ->
                R.drawable.ic_cat_game_sports
            "game_trivia" ->
                R.drawable.ic_cat_game_trivia
            "game_word" ->
                R.drawable.ic_cat_game_word
            "game_open_games" ->
                R.drawable.ic_cat_open_games
            "pwa_education" ->
                R.drawable.ic_cat_education
            "pwa_entertainment" ->
                R.drawable.ic_cat_entertainment
            "food & drink" ->
                R.drawable.ic_cat_food_nd_drink
            "pwa_lifestyle" ->
                R.drawable.ic_cat_lifestyle
            "music" ->
                R.drawable.ic_cat_game_music
            "news" ->
                R.drawable.ic_cat_news
            "pwa_games" ->
                R.drawable.ic_cat_game_action
            "reference" ->
                R.drawable.ic_cat_reference
            "pwa_shopping" ->
                R.drawable.ic_cat_shopping
            "pwa_social" ->
                R.drawable.ic_cat_social
            "pwa_sports" ->
                R.drawable.ic_cat_sports
            "travel" ->
                R.drawable.ic_cat_travel
            "pwa_business" ->
                R.drawable.ic_business
            "watch_face" ->
                R.drawable.ic_watchface
            "android_wear" ->
                R.drawable.ic_watch_apps
            else ->
                R.drawable.ic_cat_default
        }
    }

    fun provideGamesCategoryIconResource(categoryId: String): Int {
        return when (categoryId) {
            "action" ->
                R.drawable.ic_action
            "adventure" ->
                R.drawable.ic_adventure
            "arcade" ->
                R.drawable.ic_arcade
            "board" ->
                R.drawable.ic_board
            "card" ->
                R.drawable.ic_card
            "casino" ->
                R.drawable.ic_casino
            "casual" ->
                R.drawable.ic_casual
            "educational" ->
                R.drawable.ic_educational
            "music" ->
                R.drawable.ic_music
            "game_open_games", "web_games" ->
                R.drawable.ic_arcade
            "puzzle" ->
                R.drawable.ic_puzzle
            "racing" ->
                R.drawable.ic_racing
            "role_playing" ->
                R.drawable.ic_role_playing
            "simulation" ->
                R.drawable.ic_simulation
            "sports" ->
                R.drawable.ic_sports
            "strategy" ->
                R.drawable.ic_strategy
            "trivia" ->
                R.drawable.ic_trivia
            "word" ->
                R.drawable.ic_word
            else ->
                R.drawable.ic_cat_default
        }
    }
}
