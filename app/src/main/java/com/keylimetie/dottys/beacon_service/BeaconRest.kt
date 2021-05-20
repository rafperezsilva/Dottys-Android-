package com.keylimetie.dottys.beacon_service

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconsModel
import com.keylimetie.dottys.utils.Preferences
import org.json.JSONObject
import java.util.*
import kotlin.properties.Delegates

class BeaconRest(private val context: DottysBaseActivity) {
    val TAG = "BEACON EVENT"
    var isUploading: Boolean? = null

    fun recordBeaconEvent(beaconData: DottysBeaconRequestModel, observer: BeaconEventObserver) {
        val mQueue = Volley.newRequestQueue(context)
        isUploading = true
        //BaseLoader(context).showLoader()
            val jsonObject = JSONObject(beaconData.toJson())
        val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST,
                "${context.baseUrl}beaconEventSummaries/event",
                jsonObject,
                Response.Listener<JSONObject> { response ->
                     context.hideLoader()
                    Log.i("$TAG BEACON HAS RECORDED", "${response.toString(5)}")
                    observer.beaconRecord = DottysBeaconResponseModel.fromJson(response.toString(5))
                    isUploading = false
                },
                Response.ErrorListener { error ->
//                    val updates = Preferences.getLastestBeaconConnections(context)
//                    updates?.beacon = null
//                    updates?.let { Preferences.saveDataPreference(context as BaseActivity, PreferenceTypeKey.CONECTIONS_UPDATES, it?.toJson()) }
//                    isUploading = false
//                    BaseLoader(context).hideLoader()
//                    if (error.networkResponse != null) {
//                        val errorRes = ErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
//                        if (errorRes.error?.messages?.size ?: 0 > 0) {
//                            context.showMessage(errorRes.error?.messages?.first() ?: ""
//                            )
//                        }
                        Log.e("ERROR VOLLEY ", error.message, error)
                  //  }
                }) { //no semicolon or coma
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = Preferences(context).getCurrentToken() ?: ""
                return params
            }

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                print(response?.statusCode)

                return super.parseNetworkResponse(response)
            }
        }
        Log.e("BEACON EVENT", "\nURL:\n ${jsonObjectRequest.url} \nHEADERS:\n ${jsonObjectRequest.headers}\n" +
                "BODY:${jsonObjectRequest.body}")
        mQueue.add(jsonObjectRequest)
    }

    fun getBeaconAtNearLocation(observer: BeaconEventObserver, locarionId: String) {
        val mQueue = Volley.newRequestQueue(context)
         context.showLoader()
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
                "${context.baseUrl}beacons/?location=$locarionId",
                null,
                Response.Listener<JSONObject> { response ->
                     context.hideLoader()
                    val beaconData = DottysBeaconsModel.fromJson(response.toString())
                    observer.beaconData = beaconData
                    Log.i("$TAG NEAR LOCATION", response.toString(5))
                },
                Response.ErrorListener { error ->
                     context.hideLoader()
                    error.networkResponse?.let {
                        when (error.networkResponse.statusCode) {
                            401 -> {
                                 Log.e("BEACON", "${error.message}")
                            }
                        }
                        observer.beaconData = DottysBeaconsModel()
                        val errorRes = error.networkResponse.data.toString(Charsets.UTF_8)
                        Log.e("$TAG NEAR LOCATION", errorRes)
                    }
                }) { //no semicolon or coma

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = Preferences(context).getCurrentToken() ?: ""
                return params
            }
        }
        Log.i(TAG, "URL:\n ${jsonObjectRequest.url} \nHEADERS:\n ${jsonObjectRequest.headers}")
        mQueue.add(jsonObjectRequest)

    }

}

interface BeaconEventDelegate {
    fun onBeaconAtNearLocationRetrieved(beaconData: DottysBeaconsModel)

    fun onBeaconRecorded(beaconEvent: DottysBeaconResponseModel)
}


class BeaconEventObserver(lisener: BeaconEventDelegate) {
    var beaconRecord: DottysBeaconResponseModel by Delegates.observable(
            initialValue = DottysBeaconResponseModel(),
            onChange = { _, _, new -> lisener.onBeaconRecorded(new) })
    var beaconData: DottysBeaconsModel by Delegates.observable(
            initialValue = DottysBeaconsModel(),
            onChange = { _, _, new -> lisener.onBeaconAtNearLocationRetrieved(new) })

}