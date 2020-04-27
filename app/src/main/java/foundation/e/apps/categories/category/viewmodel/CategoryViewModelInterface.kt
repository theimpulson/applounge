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

package foundation.e.apps.categories.category.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Error

interface CategoryViewModelInterface {

    fun initialise(applicationManager: ApplicationManager, category: String)

    fun getApplications(): MutableLiveData<ArrayList<Application>>

    fun getScreenError(): MutableLiveData<Error>

    fun loadApplications(context: Context)
}
