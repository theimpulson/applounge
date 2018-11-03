package io.eelo.appinstaller.home.viewmodel

import android.content.Context
import android.graphics.Bitmap
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager

interface HomeViewModelInterface {

    fun getApplications(): Map<String, List<Application>>
    fun getCarouselImages(): List<Pair<Application, Bitmap>>

    fun load(onLoad: () -> Unit, context: Context, installManager: InstallManager)
}
