package foundation.e.apps.application.viewmodel

import android.content.Context
import foundation.e.apps.application.model.Application

interface ApplicationViewModelInterface {
    fun onApplicationClick(context: Context, application: Application)
}
