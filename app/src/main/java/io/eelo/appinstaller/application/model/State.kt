package io.eelo.appinstaller.application.model

import io.eelo.appinstaller.R

enum class State(val installButtonTextId: Int) {
    NOT_DOWNLOADED(R.string.action_install),
    NOT_UPDATED(R.string.action_update),
    INSTALLING(R.string.action_cancel),
    INSTALLED(R.string.action_launch);
}
