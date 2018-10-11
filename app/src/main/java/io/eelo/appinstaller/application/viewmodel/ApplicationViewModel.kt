package io.eelo.appinstaller.application.viewmodel

import android.content.Context
import android.content.Intent
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.ApplicationActivity

class ApplicationViewModel : ApplicationViewModelInterface {
    override fun onApplicationClick(context: Context, application: Application) {
        val intent = Intent(context, ApplicationActivity::class.java)
        context.startActivity(intent)
    }
}
