package com.playspinwin.dottys.beacon_service

 import android.util.Log
 import androidx.lifecycle.ViewModel
 import com.android.volley.AuthFailureError
 import com.android.volley.Response
 import com.android.volley.VolleyError
 import com.android.volley.toolbox.JsonObjectRequest
 import com.android.volley.toolbox.Volley
 import com.playspinwin.dottys.DottysBaseActivity
 import com.playspinwin.dottys.DottysErrorModel
 import com.playspinwin.dottys.ui.dashboard.models.DottysBeacon
 import com.playspinwin.dottys.ui.locations.showSnackBarMessage
 import org.json.JSONObject
 import java.util.*
 import kotlin.jvm.Throws
 import kotlin.properties.Delegates

enum class BeaconEventType{ENTER,EXIT}

class DottysBeaconViewModel: ViewModel(){
      var beaconDataObserver: DottysBeaconObserver? = null
var isUploading: Boolean? = null

     fun recordBeacon(mContext: DottysBaseActivity, beaconData:DottysBeaconRequestModel) {
        val mQueue = Volley.newRequestQueue(mContext)
        //beaconDataObserver = DottysBeaconObserver(mContext)
        val jsonObject = JSONObject(beaconData.toJson())
         mContext.showLoader()
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            mContext.baseUrl + "beaconEventSummaries/event",
            jsonObject,
            Response.Listener<JSONObject> { response ->
                mContext.hideLoader()
                    Log.d("BEACON RECORDED",response.toString())
                    val beaconSummary: DottysBeacon =
                        DottysBeacon.fromJson(
                            response.toString()
                        )
                    beaconDataObserver?.beaconData = beaconSummary
                isUploading = false
                }, object : Response.ErrorListener {
                    override fun onErrorResponse(error: VolleyError) {
                        isUploading = false
                        mContext.hideLoader()
                        if (error.networkResponse == null) {
                            return
                        }
                        val errorRes =
                            DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                        if (errorRes.error?.messages?.size ?: 0 > 0) {
                            DottysBaseActivity().showSnackBarMessage(mContext,
                                errorRes.error?.messages?.first() ?: "")
                        }
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



/* CURRENT USER PROTOCOL */
//region
interface DottysBeaconDelegates {
    fun getBeaconRecorded(beaconRecorded: DottysBeacon)

}

class DottysBeaconObserver(lisener: DottysBeaconDelegates) {
     var beaconData: DottysBeacon by Delegates.observable(
        initialValue = DottysBeacon(),
        onChange = { _, _, new -> lisener.getBeaconRecorded(new) })
}