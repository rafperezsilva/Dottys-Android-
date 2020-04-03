package com.keylimetie.dottys.login

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.dottysrewards.dottys.service.VolleyService
import com.keylimetie.dottys.DottysLoginResponseModel
import com.keylimetie.dottys.DottysRegisterModel
import com.keylimetie.dottys.PreferenceTypeKey
import com.keylimetie.dottys.R
import com.keylimetie.dottys.forgot_password.DottysForgotPasswordMainActivity
import com.keylimetie.dottys.models.DottysRewardsModel
import com.keylimetie.dottys.register.DottysRegisterActivity
import org.json.JSONObject
import kotlin.properties.Delegates


open class DottysLoginViewModel : ViewModel() {

    private var forgotPasswordTextView: TextView? = null
    var userLoginDataObserver: DottysLoginObserver? = null
//
    private var mContext: DottysLoginActivity? = null
    private var submitLoginButtom: Button? = null
    private var phantonSignupButton: Button? = null
    private var emailEditText: EditText? = null
    private var passwordEditText: EditText? = null

    fun initView(activityLogin: DottysLoginActivity) {

        val titleBar = activityLogin.actionBarView?.findViewById<TextView>(R.id.title_bar)
        titleBar?.text = "Log in"
        submitLoginButtom = activityLogin.findViewById(R.id.submit_login_button) as Button
        emailEditText = activityLogin.findViewById(R.id.email_login_edittext) as EditText
        passwordEditText = activityLogin.findViewById(R.id.password_login_edittext) as EditText
        phantonSignupButton = activityLogin.findViewById(R.id.phanton_singup_button) as Button
        forgotPasswordTextView = activityLogin.findViewById(R.id.forgot_passwords_textview) as TextView
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
        buttonsClickLiseners()
  }

    fun buttonsClickLiseners(){
        submitLoginButtom?.setOnClickListener {
            var dataLogin = DottysRegisterModel()
            dataLogin.email = emailEditText?.text?.toString()
            dataLogin.password = passwordEditText?.text?.toString()
            if (checkUserLoginInfo()) {
                loginUserRequest(dataLogin)
            }

        }

        forgotPasswordTextView?.setOnClickListener {
            val intent = Intent(mContext, DottysForgotPasswordMainActivity::class.java)
            mContext?.startActivity(intent)
        }

        phantonSignupButton?.setOnClickListener {
            val intent = Intent(mContext, DottysRegisterActivity::class.java)
            mContext?.startActivity(intent)
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
        val mQueue = Volley.newRequestQueue(mContext)
       mContext?.showLoader(mContext!!)
        val jsonObject = JSONObject(params as Map<*, *>)
        val jsonObjectRequest = object : JsonObjectRequest(Method.POST,
            mContext?.baseUrl + "users/login",
            jsonObject,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject) {
                    mContext?.hideLoader(mContext!!)

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
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    mContext?.hideLoader(mContext!!)

                    Log.e("TAG", error.message, error)
                }
            }) { //no semicolon or coma
            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                println(response.toString())

                return super.parseNetworkResponse(response)
            }
        }
        mQueue.add(jsonObjectRequest)
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
