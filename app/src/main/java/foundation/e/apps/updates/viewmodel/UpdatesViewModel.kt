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

package foundation.e.apps.updates.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.updates.model.UpdatesModel
import foundation.e.apps.utils.Error

class UpdatesViewModel : ViewModel(), UpdatesViewModelInterface {

    private val updatesModel = UpdatesModel()

    override fun initialise(applicationManager: ApplicationManager) {
        updatesModel.applicationManager = applicationManager
        if (updatesModel.applicationList.value != null &&
                updatesModel.applicationList.value!!.isEmpty()) {
            updatesModel.applicationList.value = null
        }
    }

    override fun getApplications(): MutableLiveData<ArrayList<Application>> {
        return updatesModel.applicationList
    }

    override fun getScreenError(): MutableLiveData<Error> {
        return updatesModel.screenError
    }

    override fun loadApplicationList(context: Context) {
        updatesModel.screenError.value = null
        updatesModel.loadApplicationList(context)
    }
}
