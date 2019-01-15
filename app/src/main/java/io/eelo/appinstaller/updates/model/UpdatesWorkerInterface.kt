package io.eelo.appinstaller.updates.model

import io.eelo.appinstaller.application.model.Application

interface UpdatesWorkerInterface {
    fun onApplicationsFound(applications: ArrayList<Application>)
}
