/*
 * Copyright (C) 2019-2021  E FOUNDATION
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import foundation.e.apps.MainActivity.Companion.mActivity
import foundation.e.apps.R
import foundation.e.apps.application.viewmodel.ApplicationViewModel
import foundation.e.apps.home.model.BannerApplication
import kotlinx.android.synthetic.main.image_carousel_item.view.*


class ImageCarouselAdapter(context: Context, private val bannerApplications: ArrayList<BannerApplication>) : PagerAdapter() {

    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val applicationViewModel = ApplicationViewModel()

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj as LinearLayout
    }

    override fun getCount(): Int {
        return bannerApplications.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = layoutInflater.inflate(R.layout.image_carousel_item, container, false)
        val wideImage = view.image
        val image = getRoundedCornerBitmap(bannerApplications[position].image,mActivity)
//        val resizedImage=reSizeImage(image)
        wideImage.setImageBitmap(image)
        wideImage.setOnClickListener {
           if(mActivity.showApplicationTypePreference()=="open" || mActivity.showApplicationTypePreference()=="pwa"){
               return@setOnClickListener
           }
            applicationViewModel.onApplicationClick(view.context, bannerApplications[position].application!!)
        }

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as LinearLayout)
    }

    @SuppressLint("ResourceAsColor")
    fun getRoundedCornerBitmap(bitmap: Bitmap,context:Context): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height,
                Bitmap.Config.ARGB_8888)

        val canvas = Canvas(output)
        val borderSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.toFloat(),
                context.getResources().getDisplayMetrics()).toInt()
        val cornerSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.toFloat(),
                context.getResources().getDisplayMetrics()).toInt()
        val paint = Paint()
        val rect = Rect(0, 0, output.width, output.height)
        val rectF = RectF(rect)

        // prepare canvas for transfer
        paint.setAntiAlias(true)
        paint.setColor(-0x1)
        paint.setStyle(Paint.Style.FILL)
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawRoundRect(rectF, cornerSizePx.toFloat(), cornerSizePx.toFloat(), paint)

        // draw bitmap
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(bitmap, rect, rect, paint)

        // draw border
        paint.setColor(R.color.colorDivider)
        paint.setStyle(Paint.Style.STROKE)
        paint.setStrokeWidth(borderSizePx.toFloat())
        canvas.drawRoundRect(rectF, cornerSizePx.toFloat(), cornerSizePx.toFloat(), paint)
        return output
    }
}
