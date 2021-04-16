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

package foundation.e.apps.home.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.categories.model.Category
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Error

class HomeModel : HomeModelInterface {
    val applications = MutableLiveData<LinkedHashMap<Category, ArrayList<Application>>>()
    val bannerApplications = MutableLiveData<ArrayList<BannerApplication>>()
    private var applicationManager: ApplicationManager? = null
    var screenError = MutableLiveData<Error>()

    init {
        if (applications.value == null) {
            applications.value = LinkedHashMap()
        }
        if (bannerApplications.value == null) {
            bannerApplications.value = ArrayList()
        }
    }

    override fun initialise(applicationManager: ApplicationManager) {
        this.applicationManager = applicationManager
    }

    override fun getInstallManager(): ApplicationManager {
        return applicationManager!!
    }

    override fun loadCategories(context: Context) {
        if (Common.isNetworkAvailable(context)) {
            ApplicationsLoader(this).executeOnExecutor(Common.EXECUTOR, context)
        } else {
            screenError.value = Error.NO_INTERNET
        }
    }
}
