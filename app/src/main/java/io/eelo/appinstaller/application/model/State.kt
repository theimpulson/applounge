package io.eelo.appinstaller.application.model

import io.eelo.appinstaller.R

enum class State(val installButtonTextId: Int, val isInstallButtonEnabled: Boolean) {
    NOT_DOWNLOADED(R.string.action_install, true),
    NOT_UPDATED(R.string.action_update, true),
    DOWNLOADING(R.string.state_downloading, false),
    DOWNLOADED(R.string.action_install, true),
    INSTALLING(R.string.state_installing, false),
    INSTALLED(R.string.action_launch, true);
}
