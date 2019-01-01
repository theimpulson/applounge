package io.eelo.appinstaller.applicationmanager

interface ApplicationManagerServiceConnectionCallback {
    fun onServiceBind(applicationManager: ApplicationManager)
}
