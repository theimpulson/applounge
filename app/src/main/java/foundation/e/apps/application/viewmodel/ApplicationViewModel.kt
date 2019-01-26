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
