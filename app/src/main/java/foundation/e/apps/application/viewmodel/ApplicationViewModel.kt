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

package foundation.e.apps.application.viewmodel

import android.content.Context
import android.content.Intent
import foundation.e.apps.application.model.Application
import foundation.e.apps.application.ApplicationActivity
import foundation.e.apps.utils.Constants.APPLICATION_PACKAGE_NAME_KEY

class ApplicationViewModel : ApplicationViewModelInterface {
    override fun onApplicationClick(context: Context, application: Application) {
        val intent = Intent(context, ApplicationActivity::class.java)
        intent.putExtra(APPLICATION_PACKAGE_NAME_KEY, application.packageName)
        context.startActivity(intent)
    }
}
