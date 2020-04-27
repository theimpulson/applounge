package foundation.e.apps.XAPK

import org.greenrobot.eventbus.EventBus


object EventManager {

    fun register(subscriber: Any) {
        EventBus.getDefault().register(subscriber)
    }

    fun unregister(subscriber: Any) {
        EventBus.getDefault().unregister(subscriber)
    }


}