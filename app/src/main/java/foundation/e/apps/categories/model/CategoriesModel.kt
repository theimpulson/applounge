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

package foundation.e.apps.categories.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import foundation.e.apps.api.ListCategoriesRequest
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Error
import foundation.e.apps.utils.Execute

class CategoriesModel : CategoriesModelInterface {
    val applicationsCategoriesList = MutableLiveData<ArrayList<Category>>()
    val gamesCategoriesList = MutableLiveData<ArrayList<Category>>()
    val pwasCategoriesList = MutableLiveData<ArrayList<Category>>()
    var screenError = MutableLiveData<Error>()

    init {
        if (applicationsCategoriesList.value == null) {
            applicationsCategoriesList.value = ArrayList()
        }
        if (gamesCategoriesList.value == null) {
            gamesCategoriesList.value = ArrayList()
        }
        if (pwasCategoriesList.value == null) {
            pwasCategoriesList.value = ArrayList()
        }
    }

    override fun loadCategories(context: Context) {
        lateinit var result: ListCategoriesRequest.ListCategoriesResult
        var error: Error? = null
        if (Common.isNetworkAvailable(context)) {
            Execute({
                ListCategoriesRequest().request { applicationError, listCategoriesResult ->
                    when (applicationError) {
                        null -> {
                            result = listCategoriesResult!!
                        }
                        else -> {
                            error = applicationError
                        }
                    }
                }
            }, {
                if (error == null) {
                    parseResult(result)
                }
                screenError.value = error
            })
        } else {
            screenError.value = Error.NO_INTERNET
        }
    }

    private fun parseResult(result: ListCategoriesRequest.ListCategoriesResult) {
        val apps=result.appsParseResult()
        val games=result.gameParseResult()
        applicationsCategoriesList.value = apps
        gamesCategoriesList.value = games
    }
}
