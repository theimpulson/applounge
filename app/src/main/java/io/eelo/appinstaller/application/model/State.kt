package io.eelo.appinstaller.application.model

import io.eelo.appinstaller.R

enum class State(val installButtonTextId: Int) {
    NOT_DOWNLOADED(R.string.action_install),
    NOT_UPDATED(R.string.action_update),
    DOWNLOADING(R.string.state_downloading),
    INSTALLING(R.string.state_installing),
    INSTALLED(R.string.action_launch);
}
