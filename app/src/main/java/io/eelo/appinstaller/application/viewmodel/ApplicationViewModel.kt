package io.eelo.appinstaller.application.viewmodel

import android.content.Context
import android.content.Intent
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.ApplicationActivity
import io.eelo.appinstaller.utlis.Constants.APPLICATION_PACKAGE_NAME_KEY

class ApplicationViewModel : ApplicationViewModelInterface {
    override fun onApplicationClick(context: Context, application: Application) {
        val intent = Intent(context, ApplicationActivity::class.java)
        intent.putExtra(APPLICATION_PACKAGE_NAME_KEY, application.data.packageName)
        context.startActivity(intent)
    }
}
