package com.keylimetie.dottys.ui.logout

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.models.DottysRewardsModel
import kotlinx.serialization.json.JsonArray
import org.json.JSONObject
import java.util.HashMap

  class LogoutViewModel: ViewModel() {




    fun logoutRequest(mContext: DottysMainNavigationActivity) {
        val mQueue = Volley.newRequestQueue(mContext)
        mContext.showLoader(mContext)

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.GET,
                mContext.baseUrl + "users/logout",
                null,
                object : Response.Listener<JSONObject> {
                    override fun onResponse(response: JSONObject) {
                        mContext.hideLoader(mContext)
                        mContext.finishSession(mContext)
                    }
                },
                object : Response.ErrorListener {
                    override fun onErrorResponse(error: VolleyError) {
                        mContext.hideLoader(mContext)
                        mContext.finishSession(mContext)
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
