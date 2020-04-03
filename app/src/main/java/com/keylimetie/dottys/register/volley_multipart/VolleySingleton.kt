package com.keylimetie.dottys.register.volley_multipart


import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley
import com.dottysrewards.dottys.service.LruBitmapCache


/**
 * Singleton volley to populate request into single queue.
 *
 * Sketch Project Studio
 * Created by Angga on 22/04/2016 22.58.
 */
class VolleySingleton private constructor(private val mCtx: Context) {
    private var mRequestQueue: RequestQueue?

    /**
     * Get image loader.
     *
     * @return ImageLoader
     */
    val imageLoader: ImageLoader
    // Activity or BroadcastReceiver if someone passes one in.

    /**
     * Get current request queue.
     *
     * @return RequestQueue
     */
    val requestQueue: RequestQueue?
        get() {
            if (mRequestQueue == null) {
                // getApplicationContext() is key, it keeps you from leaking the
                // Activity or BroadcastReceiver if someone passes one in.
                mRequestQueue =
                    Volley.newRequestQueue(mCtx.applicationContext)
            }
            return mRequestQueue
        }

    /**
     * Add new request depend on type like string, json object, json array request.
     *
     * @param req new request
     * @param <T> request type
    </T> */
    fun <T> addToRequestQueue(req: Request<T>?) {
        requestQueue!!.add(req)
    }

    companion object {
        private var mInstance: VolleySingleton? = null

        /**
         * Singleton construct design pattern.
         *
         * @param context parent context
         * @return single instance of VolleySingleton
         */
        @Synchronized
        fun getInstance(context: Context): VolleySingleton? {
            if (mInstance == null) {
                mInstance = VolleySingleton(context)
            }
            return mInstance
        }
    }

    /**
     * Private constructor, only initialization from getInstance.
     *
     * @param context parent context
     */
    init {
        mRequestQueue = requestQueue
        imageLoader = ImageLoader(mRequestQueue,
            object : ImageLoader.ImageCache {
                private val cache: LruCache<String, Bitmap> =
                    LruBitmapCache()//mCtx)

                override fun getBitmap(url: String): Bitmap {
                    return cache.get(url)
                }

                override fun putBitmap(url: String, bitmap: Bitmap) {
                    cache.put(url, bitmap)
                }
            })
    }
}