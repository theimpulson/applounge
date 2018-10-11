package io.eelo.appinstaller.application.viewmodel

import android.content.Context
import io.eelo.appinstaller.application.model.Application

interface ApplicationViewModelInterface {
    fun onApplicationClick(context: Context, application: Application)
}
