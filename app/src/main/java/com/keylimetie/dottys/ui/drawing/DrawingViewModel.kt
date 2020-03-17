package com.keylimetie.dottys.ui.drawing

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.ui.dashboard.DashboardFragment
import org.json.JSONObject
import kotlin.properties.Delegates

class DrawingViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is tools Fragment"
    }
    val text: LiveData<String> = _text
    var drawingObserver: DottysDrawingObserver? = null

    fun initViewSetting(fragment: Fragment, locationId: String?){
        drawingObserver = DottysDrawingObserver(fragment as DashboardFragment)
        if (locationId != null) {
                val activity =  fragment.activity as? DottysMainNavigationActivity?
                getDrawingSummary(activity!!,locationId)
            }
    }

    private fun getDrawingSummary(mContext: DottysMainNavigationActivity, locationId: String) {
        val mQueue = Volley.newRequestQueue(mContext)
        mContext.showLoader(mContext)
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
            mContext.baseUrl+"locations/"+locationId,
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject) {
                    mContext.hideLoader(mContext)

                    var user: DottysDrawingModel =
                        DottysDrawingModel.fromJson(
                            response.toString()
                        )
                    // getDrawingSummary(mContext)
                    drawingObserver?.dawingModel = user
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    mContext.hideLoader(mContext)
                     Log.e("TAG", error.message, error)
                }
            }) { //no semicolon or coma

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = mContext.getUserPreference().token!!
                return params
            }

        }

        mQueue.add(jsonObjectRequest)
    }

}

interface DottysDrawingDelegates {
    fun getDrawingUser(dawing: DottysDrawingModel)

}

class DottysDrawingObserver(lisener: DottysDrawingDelegates) {
     var dawingModel: DottysDrawingModel by Delegates.observable(
        initialValue = DottysDrawingModel(),
        onChange = { prop, old, new -> lisener.getDrawingUser(new) })
}