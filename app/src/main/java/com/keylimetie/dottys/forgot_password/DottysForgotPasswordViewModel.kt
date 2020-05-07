package com.keylimetie.dottys.forgot_password

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.res.TypedArrayUtils.getText
import androidx.lifecycle.ViewModel
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.keylimetie.dottys.DottysErrorModel
import com.keylimetie.dottys.R
import com.keylimetie.dottys.forgot_password.VerificationMethodType.EMAIL
import com.keylimetie.dottys.forgot_password.VerificationMethodType.SMS
import com.keylimetie.dottys.register.DottysProfilePictureActivity
import org.json.JSONObject
import java.util.regex.Pattern
import kotlin.properties.Delegates


open class DottysForgotPasswordViewModel : ViewModel() {
    private var submitNewPasswordButton: Button? =  null
    private var newPasswordEditText: EditText? =  null
    private var enterNewPasswordEditText: EditText? =  null
    private var editTextArray: Array<EditText?> = emptyArray()
    private var verificationCodeObserver: DottysForgotPasswordObserver? = null
    private var phoneVerificationImageview: ImageView? = null
    private var phoneVerificationTextview: TextView? = null
    private var emailVerificationTextview: TextView? = null
    private var submitForfotPassword: Button? = null
    private var emailVerificationButton: Button? = null
    private var phoneVerificationButton: Button? = null
    private var emailTextview: TextView? = null


    private var firtsEditTextCode: EditText? = null
    private var secondsEditTextCode: EditText? = null
    private var thirdEditTextCode: EditText? = null
    private var fourthEditTextCode: EditText? = null
    private var fifthEditTextCode: EditText? = null
    private var sixththEditTextCode: EditText? = null

   /* FORGOT PASSWORD VIEW */
    fun initForgotPasswordView(forgotActivity: DottysForgotPasswordMainActivity) {
        emailTextview = forgotActivity.findViewById<TextView>(R.id.email_forgot_password_edittext)
        submitForfotPassword =
            forgotActivity.findViewById<Button>(R.id.submit_forgot_password_button)
        buttonForgotClickLisener(forgotActivity)
    }

    fun buttonForgotClickLisener(forgotActivity: DottysForgotPasswordMainActivity) {
        submitForfotPassword?.setOnClickListener {
            if (emailTextview?.text?.isNotEmpty() == true && forgotActivity.isValidEmail(
                    emailTextview?.text
                )
            ) {
                var intent = Intent(forgotActivity, DottysVerificationTypeActivity::class.java)
                intent.putExtra("EMAIL_FORGOT",   emailTextview?.text.toString())
                forgotActivity.startActivity(intent)
//                val intent = Intent(forgotActivity, DottysVerificationTypeActivity::class.java)
//                intent.putExtra("EMAIL_FORGOT", emailTextview?.text ?: "")
//                forgotActivity.startActivity(intent)

            } else {
                Toast.makeText(forgotActivity, "Please, enter a valid mail.", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    /* VERIFICATION CODE VIEW */
    fun initValidationView(verificationActivity: DottysVerificationTypeActivity, email: String) {
        verificationActivity.hideLoader(verificationActivity)
        verificationCodeObserver   = DottysForgotPasswordObserver(verificationActivity)
        emailVerificationTextview  = verificationActivity.findViewById<TextView>(R.id.email_verification_password_edittext)
        phoneVerificationTextview  = verificationActivity.findViewById<TextView>(R.id.phone_verification_password_edittext)
        emailVerificationButton    = verificationActivity.findViewById<Button>(R.id.phanton_verification_email_button)
        phoneVerificationButton    = verificationActivity.findViewById<Button>(R.id.phanton_verification_phone_button)
        phoneVerificationImageview = verificationActivity.findViewById<ImageView>(R.id.phone_verification_icon)

        var params = phoneVerificationImageview?.layoutParams
        params?.width = 0
        phoneVerificationImageview?.layoutParams = params
        emailVerificationTextview?.text = email
        phoneVerificationTextview?.text = "Send a SMS to verify your account"
        phoneVerificationTextview?.textAlignment = View.TEXT_ALIGNMENT_CENTER
        buttonValidationClickLisener(verificationActivity, email)
    }

    fun buttonValidationClickLisener(
        verificationActivity: DottysVerificationTypeActivity,
        email: String
    ) {
        emailVerificationButton?.setOnClickListener {
            /*FIX
            var intent = Intent(verificationActivity, DottysEnterVerificationCodeActivity::class.java)
            intent.putExtra("EMAIL_FORGOT",   email)

            verificationActivity.startActivity(intent)*/
             resesetPassword(verificationActivity, email, EMAIL)
        }
        phoneVerificationButton?.setOnClickListener {
            resesetPassword(verificationActivity, verificationActivity.strUser, SMS)
        }
    }

    /* ENTER CODE VERIFICATION VIEW */
    fun initVerificationCodeView(verificationCodeActivity: DottysEnterVerificationCodeActivity, email:String, isRegisterView: Boolean){
        verificationCodeActivity.hideLoader(verificationCodeActivity)
        firtsEditTextCode  = verificationCodeActivity.findViewById<EditText>(R.id.firts_code_edittext)
        secondsEditTextCode  = verificationCodeActivity.findViewById<EditText>(R.id.second_code_edittext)
        thirdEditTextCode  = verificationCodeActivity.findViewById<EditText>(R.id.third_code_edittext)
        fourthEditTextCode  = verificationCodeActivity.findViewById<EditText>(R.id.fourth_code_edittext)
        fifthEditTextCode  = verificationCodeActivity.findViewById<EditText>(R.id.fifth_code_edittext)
        sixththEditTextCode  = verificationCodeActivity.findViewById<EditText>(R.id.sixthcode_edittext)
        firtsEditTextCode?.requestFocus()
        editTextArray = arrayOf(
                     firtsEditTextCode,
                    secondsEditTextCode,
                    thirdEditTextCode,
                    fourthEditTextCode,
                    fifthEditTextCode,
                    sixththEditTextCode)

        for (editTextItem in editTextArray.indices){
            editTextArray[editTextItem]?.setOnFocusChangeListener { viewEdit, hasFocus ->
                print(viewEdit.id)
                val textview = verificationCodeActivity.findViewById<EditText>(viewEdit.id)
                if (textview.text.length > 1) {
                    textview.setText(textview.text.first().toString())
                    textview.clearFocus()
                }
            }

            editTextArray[editTextItem]?.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(s: Editable?) { }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length ?: 0 > 0){
                        when(editTextItem){
                            0 -> {secondsEditTextCode?.requestFocus()}
                            1 -> {thirdEditTextCode?.requestFocus()}
                            2 -> {fourthEditTextCode?.requestFocus()}
                            3 -> {fifthEditTextCode?.requestFocus()}
                            4 -> {sixththEditTextCode?.requestFocus()}
                            5 -> {sixththEditTextCode?.clearFocus()
                                onSuccessCodeManager(verificationCodeActivity, email,isRegisterView)
                            }
                        }
                    }
                }
            })
        }
    }

    fun onSuccessCodeManager(verificationCodeActivity: DottysEnterVerificationCodeActivity, email:String,isRegisterView: Boolean){
       // Toast.makeText(verificationCodeActivity,"COMPLETE CODE", Toast.LENGTH_LONG).show()
        if (isRegisterView){
            /*GO TO VERIFY CODE */
//            var intent = Intent(verificationCodeActivity, DottysProfilePictureActivity::class.java)
//            verificationCodeActivity.startActivity(intent)/*FIXME*/
           verifyRegisterCodeSMS(verificationCodeActivity,codeCollector(editTextArray))/*FIXME*/
        } else {
            if (codeCollector(editTextArray) != "") {
                var intent =
                    Intent(verificationCodeActivity, DottysEnterPasswordActivity::class.java)
                intent.putExtra("EMAIL_FORGOT", email)
                intent.putExtra("DATA_CODE", codeCollector(editTextArray))
                intent.putExtra("VIEW_FROM_PROFILE", verificationCodeActivity.viewFromProfile)
                verificationCodeActivity.startActivity(intent)
                clearDataInFields(editTextArray)
            }
        }
    }

    fun codeFieldValidator(editTextArray: Array<EditText?>):Boolean{
        for (item in  editTextArray){
            if (item?.text?.isEmpty() == true) {
                item.requestFocus()
                return false
            }
        }
        return true
    }

    fun codeCollector(editTextArray: Array<EditText?>): String{
        var textCode = StringBuilder()
        if (codeFieldValidator(editTextArray)){
            var stringArray = ArrayList<String>()
            for(item in editTextArray){
                textCode.append(item?.text.toString())
                stringArray.add(item?.text.toString())
            }

            return textCode.toString()
        } else {
            print("CODE IS NOT COMPLETE")
        }
        return ""
    }

    fun clearDataInFields(editTextArray: Array<EditText?>): Array<EditText?>{
        var clearEditext = editTextArray
        for (item in clearEditext){
            item?.setText("")
        }
        return  clearEditext
    }
    /* NETWORK REQUEST VERIFY REGISTER CODE SMS PASSWORD  */
    fun verifyRegisterCodeSMS(
        verificationActivity: DottysEnterVerificationCodeActivity,
        verifiationCode: String

    ) {
        val mQueue = Volley.newRequestQueue(verificationActivity)
        verificationActivity.showLoader()
        val params = HashMap<String, String>()

        params["verificationKey"] = verifiationCode

        val jsonObject = JSONObject(params as Map<*, *>)
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            verificationActivity.baseUrl + "users/verifyPhone",
            jsonObject,
            Response.Listener<JSONObject> {
                verificationActivity.hideLoader(verificationActivity) },
            Response.ErrorListener { error ->
                verificationActivity.hideLoader(verificationActivity)
                val errorRes = DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                if (errorRes.error?.messages?.size ?: 0 > 0) {
                    Toast.makeText(verificationActivity, errorRes.error?.messages?.first() ?: "", Toast.LENGTH_LONG).show()
                }
                Log.e("ERROR VOLLEY ", error.message, error)
                Log.e("ERROR VOLLEY ", error.message, error)/*FIXME*/
            }) { //no semicolon or coma

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                when (response?.statusCode) {
                    200, 202 -> {
                        verificationCodeObserver?.sendVerificationRegistrationCode = true
                    }
                    else -> {
                        verificationCodeObserver?.sendVerificationRegistrationCode = false
                    }
                }

                return super.parseNetworkResponse(response)
            }

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = verificationActivity.user?.token ?: ""

                return params
            }

        }
        mQueue.add(jsonObjectRequest)
    }

    /* NETWORK REQUEST FORGOT PASSWORD  */
    fun resesetPassword(
        verificationActivity: DottysVerificationTypeActivity,
        email: String?,
        methodType: VerificationMethodType
    ) {
        val mQueue = Volley.newRequestQueue(verificationActivity)
        verificationActivity.showLoader()
        val params = HashMap<String, String>()
        var method = String()
        method = if (methodType == EMAIL) {
            "email"
        } else {
            "sms"
        }
        params["email"] = email ?: ""
        params["method"] = method
        val jsonObject = JSONObject(params as Map<*, *>)
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            verificationActivity.baseUrl + "users/resetPassword",
            jsonObject,
            Response.Listener<JSONObject> {
                verificationActivity.hideLoader(verificationActivity) },
            Response.ErrorListener { error ->
                verificationActivity.hideLoader(verificationActivity)
                if (error.networkResponse != null) {
                val errorRes =
                    DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                if (errorRes.error?.messages?.size ?: 0 > 0) {
                    Toast.makeText(
                        verificationActivity,
                        errorRes.error?.messages?.first() ?: "",
                        Toast.LENGTH_LONG
                    ).show()
                }
                }
                Log.e("ERROR VOLLEY ", error.message, error)
            }) { //no semicolon or coma

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                when (response?.statusCode) {
                    202 -> {
                        verificationCodeObserver?.isSucces = true
                    }
                    else -> {
                        verificationCodeObserver?.isSucces = false
                    }
                }

                return super.parseNetworkResponse(response)
            }


        }
        mQueue.add(jsonObjectRequest)
    }

    /* CHANGE PASWORD VIEW */
    fun initChangePasswordView(changePassActivity: DottysEnterPasswordActivity, code: String, mail: String){
        changePassActivity.hideLoader(changePassActivity)
        verificationCodeObserver = DottysForgotPasswordObserver(changePassActivity)
        newPasswordEditText  = changePassActivity.findViewById<EditText>(R.id.new_password_login_edittext)
        enterNewPasswordEditText  = changePassActivity.findViewById<EditText>(R.id.confirm_new_password_login_edittext)
        submitNewPasswordButton  = changePassActivity.findViewById<Button>(R.id.submit_enter_password_button)
        submitNewPasswordButton?.setOnClickListener {
            if (newPasswordEditText?.text.toString() == enterNewPasswordEditText?.text.toString() && changePassActivity.isValidPassword(newPasswordEditText?.text.toString())) {
                changePassword(changePassActivity, mail, newPasswordEditText?.text.toString(), code)
            } else {
                Toast.makeText(changePassActivity, "Password must match", Toast.LENGTH_LONG).show()
            }
        }
    }


    fun changePassword(
        verificationActivity: DottysEnterPasswordActivity,
        email: String, password: String, code: String
    ) {
        val mQueue = Volley.newRequestQueue(verificationActivity)
        verificationActivity.showLoader()
        val params = HashMap<String, String>()
        params["email"] = email
        params["password"] = password
        params["resetKey"] = code
        val jsonObject = JSONObject(params as Map<*, *>)
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            verificationActivity.baseUrl + "users/changePassword",
            jsonObject,
            Response.Listener<JSONObject> {
                verificationActivity.hideLoader(verificationActivity) },
            Response.ErrorListener { error ->
                verificationActivity.hideLoader(verificationActivity)
                 if (error.networkResponse !=  null) {

                     val errorRes =
                         DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                     if (errorRes.error?.messages?.size ?: 0 > 0) {
                         Toast.makeText(
                             verificationActivity,
                             errorRes.error?.messages?.first() ?: "",
                             Toast.LENGTH_LONG
                         ).show()
                     }
                 }
                     verificationActivity.finish()

                Log.e("TAG", error.message, error)
            }) { //no semicolon or coma

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                when (response?.statusCode) {
                    200 -> {
                        verificationCodeObserver?.isSuccesPassword = true
                    }
                    else -> {
                        verificationCodeObserver?.isSuccesPassword = false
                    }
                }

                return super.parseNetworkResponse(response)
            }


        }
        mQueue.add(jsonObjectRequest)
    }

    /**/
}

    /* CURRENT USER PROTOCOL */
    //region
    interface DottysForgotPasswordDelegates {
        fun sendVerificationPassword(isSucces: Boolean)
        fun sendVerificationRegistrationCode(isSucces: Boolean)
        fun changePassword(isSucces: Boolean)
    }

    class DottysForgotPasswordObserver(lisener: DottysForgotPasswordDelegates) {
        var isSucces: Boolean by Delegates.observable(
            initialValue = false,
            onChange = { prop, old, new -> lisener.sendVerificationPassword(new) })
       var isSuccesPassword: Boolean by Delegates.observable(
            initialValue = false,
            onChange = { prop, old, new -> lisener.changePassword(new) })
        var sendVerificationRegistrationCode: Boolean by Delegates.observable(
            initialValue = false,
            onChange = { prop, old, new -> lisener.sendVerificationRegistrationCode(new) })
    }
    //endregion
    enum class VerificationMethodType { EMAIL, SMS }