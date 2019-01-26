package foundation.e.apps.home

import android.content.Context
import android.widget.Scroller

class ImageCarouselScroller(context: Context) : Scroller(context) {

    private val scrollDuration = 2000

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        super.startScroll(startX, startY, dx, dy, scrollDuration)
    }

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int) {
        super.startScroll(startX, startY, dx, dy, scrollDuration)
    }
}