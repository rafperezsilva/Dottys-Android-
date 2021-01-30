package com.keylimetie.dottys.login

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModel
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.dottysrewards.dottys.service.VolleyService
import com.keylimetie.dottys.*
import com.keylimetie.dottys.forgot_password.DottysForgotPasswordMainActivity
import com.keylimetie.dottys.register.DottysRegisterActivity
import com.keylimetie.dottys.ui.locations.showSnackBarMessage
import org.json.JSONObject
import kotlin.properties.Delegates


open class DottysLoginViewModel : ViewModel() { //}, TextView.OnEditorActionListener {

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
        activityLogin.hideLoader()
        activityLogin.sharedPreferences = activityLogin.getSharedPreferences(
            PreferenceTypeKey.USER_DATA.name,
            Context.MODE_PRIVATE
        )

        activityLogin.editor = activityLogin.sharedPreferences!!.edit()
        VolleyService.initialize(mContext!!)
        buttonsClickLiseners()
  }

    fun buttonsClickLiseners(){
        submitLoginButtom?.setOnClickListener {
            var dataLogin = DottysRegisterModel()
            dataLogin.email = emailEditText?.text?.toString()
            dataLogin.password = passwordEditText?.text?.toString()
            if (checkUserLoginInfo()) {
                loginUserRequest(dataLogin,mContext ?: DottysBaseActivity())
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

    private fun checkUserLoginInfo(): Boolean {
        return when {
            emailEditText?.text?.toString() == "" -> {
                DottysBaseActivity().showSnackBarMessage(mContext ?: return false,
                    "Email field are empty"
                )
                false
            }
            passwordEditText?.text?.toString() == "" -> {
                DottysBaseActivity().showSnackBarMessage(mContext ?: return false,
                    "Password field are empty"
                )
                false
            }
            passwordEditText?.text?.length!! < 5 -> {
                DottysBaseActivity().showSnackBarMessage(mContext ?: return false,
                    "Password too short"
                )
                false
            }
            else -> true
        }
    }

    fun loginUserRequest(modelRegister: DottysRegisterModel, mContext: DottysBaseActivity) {
        val params = HashMap<String, String>()
        params["email"] = modelRegister.email!!
        params["password"] = modelRegister.password!!
        val mQueue = Volley.newRequestQueue(mContext)
       mContext?.showLoader()
        val jsonObject = JSONObject(params as Map<*, *>)
        val jsonObjectRequest = object : JsonObjectRequest(Method.POST,
            mContext?.baseUrl + "users/login",
            jsonObject,
            Response.Listener<JSONObject> { response ->
                mContext?.hideLoader()
                Log.d("LOGIN RESPONSE", response.toString())
                try {
                    var person: DottysLoginResponseModel =
                        DottysLoginResponseModel.fromJson(
                            response.toString()
                        )
                    userLoginDataObserver?.registerUserModel = person
                } catch (e: Exception) {
                    println(e)
                    mContext?.hideLoader()
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    mContext?.hideLoader()
                    mContext.hideCustomKeyboard(mContext)
                    if (error.networkResponse == null) {
                        mContext.showSnackBarMessage(mContext,
                            "Please, check your internet connection."
                        )
                        return
                    }
                    val errorRes =
                        DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                    if (errorRes.error?.messages?.size ?: 0 > 0) {
                        mContext.showSnackBarMessage(mContext,
                            errorRes.error?.messages?.first() ?: ""
                        )
                    }
                    Log.e("TAG", error.message, error)
                }
            }) { //no semicolon or coma
            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                Log.d("ON LOGIN",response.toString())

                return super.parseNetworkResponse(response)
            }
        }
        mQueue.add(jsonObjectRequest)
    }

//    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
//        if(actionId == EditorInfo.IME_ACTION_DONE){
//            var msg = ""
//            when(v?.id) {
//                 R.id.email_login_edittext -> {
//                     msg = "From Emal"
//                 }
//                  R.id.password_login_edittext -> {
//                      msg = "From Pasword"
//                  }
//            }
//            DottysBaseActivity().showSnackBarMessage(this,mContext, msg)
//        }
//        return    false
//    }
}


interface DottysLoginDelegate {
    fun onUserLogin(registerUserData: DottysLoginResponseModel)
}

class DottysLoginObserver(lisener: DottysLoginDelegate) {
    var registerUserModel: DottysLoginResponseModel by Delegates.observable(
        initialValue = DottysLoginResponseModel(),
        onChange = { _, _, new -> lisener.onUserLogin(new) })
}
