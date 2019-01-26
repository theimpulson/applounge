package foundation.e.apps.updates.model

import foundation.e.apps.application.model.Application

interface UpdatesWorkerInterface {
    fun onApplicationsFound(applications: ArrayList<Application>)
}
