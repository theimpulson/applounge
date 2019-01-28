package foundation.e.apps.application.model

import foundation.e.apps.R

enum class State(val installButtonTextId: Int) {
    NOT_DOWNLOADED(R.string.action_install),
    NOT_UPDATED(R.string.action_update),
    DOWNLOADING(R.string.action_cancel),
    INSTALLING(R.string.state_installing),
    INSTALLED(R.string.action_launch);
}
