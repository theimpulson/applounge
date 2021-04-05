/*
 * Copyright (C) 2019-2021  E FOUNDATION
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

package foundation.e.apps.search.model

import android.content.Context
import android.os.AsyncTask
import foundation.e.apps.api.AllAppsSearchRequest
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Constants

class SearchSuggestionsTask(private val searchQuery: String,
                            private val applicationManager: ApplicationManager,
                            private val callback: SearchModelInterface)
    : AsyncTask<Context, Void, ArrayList<String>>() {

    override fun doInBackground(vararg context: Context): ArrayList<String> {
        val searchSuggestions = ArrayList<String>()

        AllAppsSearchRequest(searchQuery, 1, Constants.SUGGESTIONS_RESULTS)
                .request { applicationError, searchResult ->
                    when (applicationError) {
                        null -> {
                            val applications = searchResult!!.getApplications(applicationManager, context[0])
                            applications.forEach {
                                searchSuggestions.add(it.searchAppsBasicData!!.name)
                            }
                        }
                        else -> {
                            // Do nothing
                        }
                    }
                }

        return searchSuggestions
    }

    override fun onPostExecute(result: ArrayList<String>) {
        /*User type following string it will add microG element into array list*/
        if ("microG Exposure Notification version".contains(searchQuery, true)) {
            result.add(0,"microG Exposure Notification version")
        }
        callback.onSearchSuggestionsRetrieved(searchQuery, result)
    }
}
