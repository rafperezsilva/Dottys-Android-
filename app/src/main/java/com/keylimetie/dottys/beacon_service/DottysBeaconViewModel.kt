package com.keylimetie.dottys.beacon_service

 import android.widget.Toast
 import androidx.lifecycle.ViewModel
 import com.android.volley.AuthFailureError
 import com.android.volley.Response
 import com.android.volley.VolleyError
 import com.android.volley.toolbox.JsonObjectRequest
 import com.android.volley.toolbox.Volley
 import com.keylimetie.dottys.DottysBaseActivity
 import com.keylimetie.dottys.DottysErrorModel
 import org.json.JSONObject
 import java.util.*
 import kotlin.properties.Delegates

enum class BeaconEventType{ENTER,EXIT}
class DottysBeaconViewModel: ViewModel(){
    private var beaconDataObserver: DottysBeaconObserver? = null


     fun recordBeacon(mContext: DottysBaseActivity, beaconData:DottysBeaconRequestModel) {
        val mQueue = Volley.newRequestQueue(mContext)
        beaconDataObserver = DottysBeaconObserver(mContext)
        val jsonObject = JSONObject(beaconData.toJson())
        mContext.showLoader()
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            mContext.baseUrl + "beaconEventSummaries/event",
            jsonObject,
            Response.Listener<JSONObject> { response ->
                mContext.hideLoader()
                    println(response.toString())
                    val beaconSummary: DottysBeaconResponseModel =
                        DottysBeaconResponseModel.fromJson(
                            response.toString()
                        )
                    beaconDataObserver?.beaconData = beaconSummary
                }, object : Response.ErrorListener {
                    override fun onErrorResponse(error: VolleyError) {
                        mContext.hideLoader()
                        if (error.networkResponse == null) {
                            return
                        }
                        val errorRes =
                            DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                        if (errorRes.error?.messages?.size ?: 0 > 0) {
                            Toast.makeText(
                                mContext,
                                errorRes.error?.messages?.first() ?: "",
                                Toast.LENGTH_LONG
                            ).show()
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
    fun getBeaconRecorded(beaconRecorded: DottysBeaconResponseModel)

}

class DottysBeaconObserver(lisener: DottysBeaconDelegates) {
     var beaconData: DottysBeaconResponseModel by Delegates.observable(
        initialValue = DottysBeaconResponseModel(),
        onChange = { _, _, new -> lisener.getBeaconRecorded(new) })
}