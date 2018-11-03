package io.eelo.appinstaller.home.model

import android.content.Context
import io.eelo.appinstaller.application.model.InstallManager

interface HomeModelInterface {
    fun load(onLoad: () -> Unit, context: Context, installManager: InstallManager)
}