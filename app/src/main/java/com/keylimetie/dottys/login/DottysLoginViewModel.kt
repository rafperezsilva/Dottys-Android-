package com.keylimetie.dottys.login

import android.content.Context
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.dottysrewards.dottys.service.VolleyService
import com.keylimetie.dottys.DottysLoginResponseModel
import com.keylimetie.dottys.DottysRegisterModel
import com.keylimetie.dottys.PreferenceTypeKey
import com.keylimetie.dottys.R
import org.json.JSONObject
import kotlin.properties.Delegates

open class DottysLoginViewModel : ViewModel() {

     var userLoginDataObserver: DottysLoginObserver? = null
//
    private var mContext: DottysLoginActivity? = null
    private var submitLoginButtom: Button? = null
    private var emailEditText: EditText? = null
    private var passwordEditText: EditText? = null

    fun initView(activityLogin: DottysLoginActivity) {


        val titleBar = activityLogin.actionBarView?.findViewById<TextView>(R.id.title_bar)
        titleBar?.text = "Log in"
        submitLoginButtom = activityLogin.findViewById(R.id.submit_login_button) as Button
        emailEditText = activityLogin.findViewById(R.id.email_login_edittext) as EditText
        passwordEditText = activityLogin.findViewById(R.id.password_login_edittext) as EditText
        mContext = activityLogin
        userLoginDataObserver = DottysLoginObserver(activityLogin)
        activityLogin.hideLoader(activityLogin)
        activityLogin.sharedPreferences = activityLogin.getSharedPreferences(
            PreferenceTypeKey.USER_DATA.name,
            Context.MODE_PRIVATE
        )
        activityLogin.editor = activityLogin.sharedPreferences!!.edit()
        VolleyService.initialize(mContext!!)
        //userRegisterData = DottysRegisterObservers(activityRegister)


        submitLoginButtom?.setOnClickListener {
            var dataLogin = DottysRegisterModel()
            dataLogin.email = emailEditText?.text?.toString()
            dataLogin.password = passwordEditText?.text?.toString()
            if (checkUserLoginInfo()) {
                loginUserRequest(dataLogin)
            }

        }
    }

    fun checkUserLoginInfo(): Boolean {
        if (emailEditText?.text?.toString() == "") {
            Toast.makeText(mContext, "Email field are empty", Toast.LENGTH_SHORT).show()
            return false
        } else if (passwordEditText?.text?.toString() == "") {
            Toast.makeText(mContext, "Password field are empty", Toast.LENGTH_SHORT).show()
            return false
        } else if (passwordEditText?.text?.length!! < 5) {
            Toast.makeText(mContext, "Password too short", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun loginUserRequest(modelRegister: DottysRegisterModel) {
        val params = HashMap<String, String>()
        params["email"] = modelRegister.email!!
        params["password"] = modelRegister.password!!
        val jsonObject = JSONObject(params as Map<*, *>)
       mContext?.showLoader(mContext!!)
        var request: JsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            mContext?.baseUrl + "users/login",
            jsonObject,
            Response.Listener { response ->
                mContext?.hideLoader(mContext!!)
                // Process the json
                try {
                    var person: DottysLoginResponseModel =
                        DottysLoginResponseModel.fromJson(
                            response.toString()
                        )
                    userLoginDataObserver?.registerUserModel = person
                   Toast.makeText(mContext, "FULL NAME: \n" + person.fullName, Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    println(e)
                    mContext?.hideLoader(mContext!!)
                }

            },
            Response.ErrorListener {
                if (it.networkResponse != null && it.networkResponse.statusCode == 400) {
                    Toast.makeText(mContext, "Invalid username or password.", Toast.LENGTH_LONG)
                        .show()
                }
                mContext?.hideLoader(mContext!!)
                println("Volley error: $it")
            })

        request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            0,
            1f
        )

        VolleyService.requestQueue.add(request)
        VolleyService.requestQueue.start()
    }
}


interface DottysLoginDelegate {
    fun onUserLogin(registerUserData: DottysLoginResponseModel)
}

class DottysLoginObserver(lisener: DottysLoginDelegate) {
    var registerUserModel: DottysLoginResponseModel by Delegates.observable(
        initialValue = DottysLoginResponseModel(),
        onChange = { _, _, new -> lisener.onUserLogin(new) })
}