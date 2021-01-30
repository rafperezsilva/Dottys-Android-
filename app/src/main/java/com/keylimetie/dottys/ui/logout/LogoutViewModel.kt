package com.keylimetie.dottys.ui.logout

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.DottysErrorModel
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.ui.locations.showSnackBarMessage
import org.json.JSONObject
import java.util.*
import kotlin.jvm.Throws

class LogoutViewModel: ViewModel() {




    fun logoutRequest(mContext: DottysMainNavigationActivity) {
        val mQueue = Volley.newRequestQueue(mContext)
        mContext.showLoader()

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.GET,
                mContext.baseUrl + "users/logout",
                null,
                object : Response.Listener<JSONObject> {
                    override fun onResponse(response: JSONObject) {
                        mContext.hideLoader()
                        mContext.finishSession(mContext)
                    }
                },
                object : Response.ErrorListener {
                    override fun onErrorResponse(error: VolleyError) {
                        mContext.hideLoader()
                        mContext.finishSession(mContext)
                        if (error.networkResponse ==  null){return}
                        val errorRes =
                            DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                        if (errorRes.error?.messages?.size ?: 0 > 0) {
                            DottysBaseActivity().showSnackBarMessage(mContext,
                                errorRes.error?.messages?.first() ?: "")
                        }
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
