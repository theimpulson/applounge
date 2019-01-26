package foundation.e.apps.home

import android.os.Handler
import android.support.v4.view.ViewPager

class ImageCarouselSwitcher(private var imagesCount: Int, private val imageCarousel: ViewPager) {
    private val handler = Handler()
    private val switchImageInterval = 4000L

    fun start() {
        handler.postDelayed(this::switchImage, switchImageInterval)
    }

    private fun switchImage() {
        if (isAllImagesSubmitted()) {
            imageCarousel.setCurrentItem(0, true)
        } else {
            imageCarousel.setCurrentItem(imageCarousel.currentItem + 1, true)
            handler.postDelayed(this::switchImage, switchImageInterval)
        }
    }

    private fun isAllImagesSubmitted(): Boolean {
        return imageCarousel.currentItem == imagesCount - 1
    }
}