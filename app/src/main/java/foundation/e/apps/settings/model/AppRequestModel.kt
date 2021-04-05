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

package foundation.e.apps.settings.model

import android.content.Context
import android.os.AsyncTask
import androidx.lifecycle.MutableLiveData
import foundation.e.apps.api.AppRequestRequest
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Error

class AppRequestModel : AppRequestModelInterface {
    var screenError = MutableLiveData<Error>()

    override fun onSubmit(context: Context, packageName: String) {
        screenError.value = null
        if (Common.isNetworkAvailable(context)) {
            AppRequestTask(packageName, this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        } else {
            screenError.value = Error.NO_INTERNET
        }
    }

    override fun onAppRequested(error: Error?) {
        screenError.value = error
    }
}

class AppRequestTask(
        private val packageName: String,
        private val callback: AppRequestModelInterface) :
        AsyncTask<Void, Void, Error?>() {

    override fun doInBackground(vararg p0: Void?): Error? {
        var error: Error? = null
        AppRequestRequest().request(packageName) {
            error = it
        }
        return error
    }

    override fun onPostExecute(result: Error?) {
        callback.onAppRequested(result)
    }
}
