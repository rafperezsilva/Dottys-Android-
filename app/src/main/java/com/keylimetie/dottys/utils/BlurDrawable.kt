package com.keylimetie.dottys.utils

import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import java.lang.ref.WeakReference
import java.util.*

/**
 * A drawable that draws the target view as blurred using fast blur
 *
 *
 *
 *
 * TODO:we might use setBounds() to draw only part a of the target view
 *
 *
 * Created by 10uR on 24.5.2014.
 */
internal class BlurDrawable @JvmOverloads constructor(target: View, radius: Int = 10) :
    Drawable() {
    private val targetRef: WeakReference<View>
    private var blurred: Bitmap? = null
    private val paint: Paint
    private var radius = 0
    override fun draw(canvas: Canvas) {
        if (blurred == null) {
            val target = targetRef.get()
            if (target != null) {
                val bitmap = target.getDrawingCache(true) ?: return
                blurred = fastBlur(bitmap, radius)
            }
        }
        if (blurred != null && !blurred!!.isRecycled) canvas.drawBitmap(blurred!!, 0f, 0f, paint)
    }

    fun getRadius(): Int {
        return radius
    }

    /**
     * Set the bluring radius that will be applied to target view's bitmap
     *
     * @param radius should be 0-100
     */
    fun setRadius(radius: Int) {
        if (radius < 0 || radius > 100) throw InputMismatchException("Radius must be 0 <= radius <= 100 !")
        this.radius = radius
        if (blurred != null) {
            blurred!!.recycle()
            blurred = null
        }
        invalidateSelf()
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(cf: ColorFilter?) {}
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    companion object {
        /**
         * from https://stackoverflow.com/a/10028267/3133545
         *
         *
         *
         *
         *
         *
         * Stack Blur v1.0 from
         * http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
         *
         *
         * Java Author: Mario Klingemann <mario at quasimondo.com>
         * http://incubator.quasimondo.com
         * created Feburary 29, 2004
         * Android port : Yahel Bouaziz <yahel at kayenko.com>
         * http://www.kayenko.com
         * ported april 5th, 2012
         *
         *
         * This is a compromise between Gaussian Blur and Box blur
         * It creates much better looking blurs than Box Blur, but is
         * 7x faster than my Gaussian Blur implementation.
         *
         *
         * I called it Stack Blur because this describes best how this
         * filter works internally: it creates a kind of moving stack
         * of colors whilst scanning through the image. Thereby it
         * just has to add one new block of color to the right side
         * of the stack and remove the leftmost color. The remaining
         * colors on the topmost layer of the stack are either added on
         * or reduced by one, depending on if they are on the right or
         * on the left side of the stack.
         *
         *
         * If you are using this algorithm in your code please add
         * the following line:
         *
         *
         * Stack Blur Algorithm by Mario Klingemann <mario></mario>@quasimondo.com>
        </yahel></mario> */
        private fun fastBlur(sentBitmap: Bitmap, radius: Int): Bitmap? {
            val bitmap = sentBitmap.copy(sentBitmap.config, true)
            if (radius < 1) {
                return null
            }
            val w = bitmap.width
            val h = bitmap.height
            val pix = IntArray(w * h)
            Log.e("pix", w.toString() + " " + h + " " + pix.size)
            bitmap.getPixels(pix, 0, w, 0, 0, w, h)
            val wm = w - 1
            val hm = h - 1
            val wh = w * h
            val div = radius + radius + 1
            val r = IntArray(wh)
            val g = IntArray(wh)
            val b = IntArray(wh)
            var rsum: Int
            var gsum: Int
            var bsum: Int
            var x: Int
            var y: Int
            var i: Int
            var p: Int
            var yp: Int
            var yi: Int
            var yw: Int
            val vmin = IntArray(Math.max(w, h))
            var divsum = div + 1 shr 1
            divsum *= divsum
            val dv = IntArray(256 * divsum)
            i = 0
            while (i < 256 * divsum) {
                dv[i] = i / divsum
                i++
            }
            yi = 0
            yw = yi
            val stack = Array(div) { IntArray(3) }
            var stackpointer: Int
            var stackstart: Int
            var sir: IntArray
            var rbs: Int
            val r1 = radius + 1
            var routsum: Int
            var goutsum: Int
            var boutsum: Int
            var rinsum: Int
            var ginsum: Int
            var binsum: Int
            y = 0
            while (y < h) {
                bsum = 0
                gsum = bsum
                rsum = gsum
                boutsum = rsum
                goutsum = boutsum
                routsum = goutsum
                binsum = routsum
                ginsum = binsum
                rinsum = ginsum
                i = -radius
                while (i <= radius) {
                    p = pix[yi + Math.min(wm, Math.max(i, 0))]
                    sir = stack[i + radius]
                    sir[0] = p and 0xff0000 shr 16
                    sir[1] = p and 0x00ff00 shr 8
                    sir[2] = p and 0x0000ff
                    rbs = r1 - Math.abs(i)
                    rsum += sir[0] * rbs
                    gsum += sir[1] * rbs
                    bsum += sir[2] * rbs
                    if (i > 0) {
                        rinsum += sir[0]
                        ginsum += sir[1]
                        binsum += sir[2]
                    } else {
                        routsum += sir[0]
                        goutsum += sir[1]
                        boutsum += sir[2]
                    }
                    i++
                }
                stackpointer = radius
                x = 0
                while (x < w) {
                    r[yi] = dv[rsum]
                    g[yi] = dv[gsum]
                    b[yi] = dv[bsum]
                    rsum -= routsum
                    gsum -= goutsum
                    bsum -= boutsum
                    stackstart = stackpointer - radius + div
                    sir = stack[stackstart % div]
                    routsum -= sir[0]
                    goutsum -= sir[1]
                    boutsum -= sir[2]
                    if (y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm)
                    }
                    p = pix[yw + vmin[x]]
                    sir[0] = p and 0xff0000 shr 16
                    sir[1] = p and 0x00ff00 shr 8
                    sir[2] = p and 0x0000ff
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                    rsum += rinsum
                    gsum += ginsum
                    bsum += binsum
                    stackpointer = (stackpointer + 1) % div
                    sir = stack[stackpointer % div]
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                    rinsum -= sir[0]
                    ginsum -= sir[1]
                    binsum -= sir[2]
                    yi++
                    x++
                }
                yw += w
                y++
            }
            x = 0
            while (x < w) {
                bsum = 0
                gsum = bsum
                rsum = gsum
                boutsum = rsum
                goutsum = boutsum
                routsum = goutsum
                binsum = routsum
                ginsum = binsum
                rinsum = ginsum
                yp = -radius * w
                i = -radius
                while (i <= radius) {
                    yi = Math.max(0, yp) + x
                    sir = stack[i + radius]
                    sir[0] = r[yi]
                    sir[1] = g[yi]
                    sir[2] = b[yi]
                    rbs = r1 - Math.abs(i)
                    rsum += r[yi] * rbs
                    gsum += g[yi] * rbs
                    bsum += b[yi] * rbs
                    if (i > 0) {
                        rinsum += sir[0]
                        ginsum += sir[1]
                        binsum += sir[2]
                    } else {
                        routsum += sir[0]
                        goutsum += sir[1]
                        boutsum += sir[2]
                    }
                    if (i < hm) {
                        yp += w
                    }
                    i++
                }
                yi = x
                stackpointer = radius
                y = 0
                while (y < h) {

                    // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                    pix[yi] =
                        -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                    rsum -= routsum
                    gsum -= goutsum
                    bsum -= boutsum
                    stackstart = stackpointer - radius + div
                    sir = stack[stackstart % div]
                    routsum -= sir[0]
                    goutsum -= sir[1]
                    boutsum -= sir[2]
                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w
                    }
                    p = x + vmin[y]
                    sir[0] = r[p]
                    sir[1] = g[p]
                    sir[2] = b[p]
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                    rsum += rinsum
                    gsum += ginsum
                    bsum += binsum
                    stackpointer = (stackpointer + 1) % div
                    sir = stack[stackpointer]
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                    rinsum -= sir[0]
                    ginsum -= sir[1]
                    binsum -= sir[2]
                    yi += w
                    y++
                }
                x++
            }
            bitmap.setPixels(pix, 0, w, 0, 0, w, h)
            return bitmap
        }
    }

    init {
        targetRef = WeakReference(target)
        setRadius(radius)
        target.isDrawingCacheEnabled = true
        target.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_AUTO
        paint = Paint()
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
    }
}