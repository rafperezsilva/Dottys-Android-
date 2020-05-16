package com.keylimetie.dottys.beacon_service

 import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
 import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.DottysErrorModel
import com.keylimetie.dottys.ui.dashboard.models.DottysDrawingSumaryModel
 import org.json.JSONObject
import java.util.HashMap
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
                    mContext.hideLoader(mContext)
                    println(response.toString())
                    val beaconSummary: DottysBeaconResponseModel =
                        DottysBeaconResponseModel.fromJson(
                            response.toString()
                        )
                    beaconDataObserver?.beaconData = beaconSummary
                }, object : Response.ErrorListener {
                    override fun onErrorResponse(error: VolleyError) {
                        mContext.hideLoader(mContext)
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