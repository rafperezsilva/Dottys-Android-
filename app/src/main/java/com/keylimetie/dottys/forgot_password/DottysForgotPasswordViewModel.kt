package com.keylimetie.dottys.forgot_password

import android.annotation.SuppressLint
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModel
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.models.DottysErrorModel
import com.keylimetie.dottys.R
import com.keylimetie.dottys.forgot_password.VerificationMethodType.EMAIL
import com.keylimetie.dottys.forgot_password.VerificationMethodType.SMS
import com.keylimetie.dottys.register.DottysRegisterViewModel
import com.keylimetie.dottys.ui.locations.showSnackBarMessage
import com.keylimetie.dottys.utils.isValidEmail
import com.keylimetie.dottys.utils.isValidPassword
import org.json.JSONObject
import kotlin.math.roundToInt
import kotlin.properties.Delegates


open class DottysForgotPasswordViewModel : ViewModel()  {
    private var submitNewPasswordButton: Button? =  null
    private var newPasswordEditText: EditText? =  null
    private var enterNewPasswordEditText: EditText? =  null
    var editTextArray: Array<EditText?> = emptyArray()
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
    private var isRegisterView: Boolean = false

    private var isSmsView: Boolean? = null


    /* FORGOT PASSWORD VIEW */
    fun initForgotPasswordView(forgotActivity: DottysForgotPasswordMainActivity) {
        emailTextview = forgotActivity.findViewById<TextView>(R.id.email_forgot_password_edittext)
        submitForfotPassword =
            forgotActivity.findViewById<Button>(R.id.submit_forgot_password_button)
        buttonForgotClickLisener(forgotActivity)
    }

    fun fillCodeVerification(smsCode:String){
        if(smsCode.length >= editTextArray.size){
            for((index,et) in editTextArray.withIndex()){
                et?.setText(smsCode[index].toString())
            }
        }
    }
    fun buttonForgotClickLisener(forgotActivity: DottysForgotPasswordMainActivity) {
        submitForfotPassword?.setOnClickListener {
            if (emailTextview?.text?.isNotEmpty() == true &&
                    emailTextview?.text.toString().isValidEmail())
             {
                var intent = Intent(forgotActivity, DottysVerificationTypeActivity::class.java)
                intent.putExtra("EMAIL_FORGOT",   emailTextview?.text.toString())
                forgotActivity.startActivity(intent)
//                val intent = Intent(forgotActivity, DottysVerificationTypeActivity::class.java)
//                intent.putExtra("EMAIL_FORGOT", emailTextview?.text ?: "")
//                forgotActivity.startActivity(intent)

            } else {
                DottysBaseActivity().showSnackBarMessage(forgotActivity,
                    "Please, enter a valid mail."
                )
            }
        }
    }

    /**
     *  VERIFICATION CODE VIEW */
    fun initValidationView(verificationActivity: DottysVerificationTypeActivity, email: String) {
        verificationActivity.hideLoader()
        verificationCodeObserver   = DottysForgotPasswordObserver(verificationActivity)
        emailVerificationTextview  = verificationActivity.findViewById<TextView>(R.id.email_verification_password_edittext)
        phoneVerificationTextview  = verificationActivity.findViewById<TextView>(R.id.phone_verification_password_edittext)
        emailVerificationButton    = verificationActivity.findViewById<Button>(R.id.phanton_verification_email_button)
        phoneVerificationButton    = verificationActivity.findViewById<Button>(R.id.phanton_verification_phone_button)
        phoneVerificationImageview = verificationActivity.findViewById<ImageView>(R.id.phone_verification_icon)

        var params = phoneVerificationImageview?.layoutParams
        params?.width = 0
        val newName = email.split("@")[0]
        var index : Int = 0
        if (newName.chars().count() > 1){
            index = (newName.chars().count() / 2).toFloat().roundToInt()

        }
        val builder = StringBuilder()
         for(char in index until  newName.chars().count()) {
            builder.append("x")
        }
        val mailUser = builder?.let { newName.replaceRange(index, newName.lastIndex, it) } + "@${email.split("@")[1]}"
        phoneVerificationImageview?.layoutParams = params
        emailVerificationTextview?.text =  mailUser
        val phone = verificationActivity.getUserPreference().cell
        val mssg = if (phone.isNullOrEmpty()) "Send a SMS to your phone" else {
            val number = phone.subSequence(phone.count() - 4, phone.count())
            "Send a SMS to (xxx) xxx-$number"
        }
        phoneVerificationTextview?.text = mssg//(XXX) XXX-${phone?.substring((phone?.chars()?.count() ?: 0).toInt() - 4, phone?.chars()?.count()?.toInt() ?: 0)}"
        phoneVerificationTextview?.textAlignment = View.TEXT_ALIGNMENT_CENTER
        buttonValidationClickLisener(verificationActivity, email)
    }

    private fun buttonValidationClickLisener(
        verificationActivity: DottysVerificationTypeActivity,
        email: String
    ) {
        emailVerificationButton?.setOnClickListener {
            /*FIX
            var intent = Intent(verificationActivity, DottysEnterVerificationCodeActivity::class.java)
            intent.putExtra("EMAIL_FORGOT",   email)

            verificationActivity.startActivity(intent)*/
            isSmsView = false
             resesetPassword(verificationActivity, email, EMAIL)
        }
        phoneVerificationButton?.setOnClickListener {
            isSmsView = true
            resesetPassword(verificationActivity, verificationActivity.strUser, SMS)
        }
    }

    /** ENTER CODE VERIFICATION VIEW */
    @SuppressLint("SetTextI18n")
    fun initVerificationCodeView(verificationCodeActivity: DottysEnterVerificationCodeActivity, email:String, isRegisterView: Boolean){
        verificationCodeActivity.hideLoader()
        this.isRegisterView = isRegisterView
        firtsEditTextCode  = verificationCodeActivity.findViewById<EditText>(R.id.firts_code_edittext)
        secondsEditTextCode  = verificationCodeActivity.findViewById<EditText>(R.id.second_code_edittext)
        thirdEditTextCode  = verificationCodeActivity.findViewById<EditText>(R.id.third_code_edittext)
        fourthEditTextCode  = verificationCodeActivity.findViewById<EditText>(R.id.fourth_code_edittext)
        fifthEditTextCode  = verificationCodeActivity.findViewById<EditText>(R.id.fifth_code_edittext)
        sixththEditTextCode  = verificationCodeActivity.findViewById<EditText>(R.id.sixthcode_edittext)
        val subtitleVerification   = verificationCodeActivity.findViewById<TextView>(R.id.subtitle_verification_code_textview)
        val resendCodeTextView  = verificationCodeActivity.findViewById<TextView>(R.id.resend_code_textview)
        val phone = verificationCodeActivity.getUserPreference().cell
        val number = if(phone.isNullOrEmpty()) "" else phone?.subSequence(phone.count() - 4, phone.count())
        val msg = if(isSmsView ?: true) "A text message " else "A email "
        subtitleVerification.text = "$msg with a verification code\nhas been sent to ${
            if (number.isEmpty()) {
                "your phone"
            } else {
                "(xxx) xxx-$number"
            }
        }"

        resendCodeTextView.setOnClickListener {
            val registerMOdel = DottysRegisterViewModel(verificationCodeActivity)
            phone?.let { it1 ->
                registerMOdel.requestNewVerificationPhone(verificationCodeActivity,
                    it1
                )
            }
        }
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
                            0 -> {
                                secondsEditTextCode?.setText("")
                                secondsEditTextCode?.requestFocus()

                            }
                            1 -> {
                                thirdEditTextCode?.setText("")
                                thirdEditTextCode?.requestFocus()
                            }
                            2 -> {
                                fourthEditTextCode?.setText("")
                                fourthEditTextCode?.requestFocus()
                            }
                            3 -> {
                                fifthEditTextCode?.setText("")
                                fifthEditTextCode?.requestFocus()
                            }
                            4 -> {
                                sixththEditTextCode?.setText("")
                                sixththEditTextCode?.requestFocus()
                            }
                            5 -> {
                                sixththEditTextCode?.clearFocus()
                                onSuccessCodeManager(verificationCodeActivity, email,isRegisterView)
                            }
                        }
                    }
                }
            })
        }
    }

    fun onSuccessCodeManager(verificationCodeActivity: DottysEnterVerificationCodeActivity, email:String,isRegisterView: Boolean){
        // DottysBaseActivity().showSnackBarMessage(this,verificationCodeActivity,"COMPLETE CODE")
        if (isRegisterView){
            /*GO TO VERIFY CODE */
//            var intent = Intent(verificationCodeActivity, DottysProfilePictureActivity::class.java)
//            verificationCodeActivity.startActivity(intent)/*FIXME*/
           verifyRegisterCodeSMS(verificationCodeActivity,codeCollector(editTextArray),email)
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
        verifiationCode: String,
        email:String
    ) {
        val mQueue = Volley.newRequestQueue(verificationActivity)
        verificationActivity.showLoader()
        val params = HashMap<String, String>()
        verificationCodeObserver = DottysForgotPasswordObserver(verificationActivity)
        params["verificationKey"] = verifiationCode
        params["email"] = email

        val jsonObject = JSONObject(params as Map<*, *>)
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            verificationActivity.baseUrl + "users/verifyPhone",
            jsonObject,
            Response.Listener<JSONObject> {
                verificationActivity.hideLoader()
            },
            Response.ErrorListener { error ->
                verificationActivity.hideLoader()
                verificationActivity.hideCustomKeyboard(verificationActivity)
                try {
                    val errorRes =
                        DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                    if (errorRes.error?.messages?.size ?: 0 > 0) {
                        DottysBaseActivity().showSnackBarMessage(
                            verificationActivity,
                            errorRes.error?.messages?.first() ?: ""
                        )
                    }
                    Log.e("ERROR VOLLEY ", error.message, error)
                } catch (e:Exception){Log.e("FORGOT_PASSWORD_VM","CAST ERROR ${e.message}")}
                verificationCodeObserver?.sendVerificationRegistrationCode = false
            }) { //no semicolon or coma

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
               // clearDataInFields(editTextArray)
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

            override fun getHeaders(): MutableMap<String, String>  {
                val params = HashMap<String, String>()
                params["Authorization"] =  if  (verificationActivity.user?.token.isNullOrEmpty())
                    verificationActivity.getUserPreference().token  ?: ""
                else
                    verificationActivity.user?.token ?: ""

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
                verificationActivity.hideLoader()
            },
            Response.ErrorListener { error ->
                verificationActivity.hideLoader()
                if (error.networkResponse != null) {
                val errorRes =
                    DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                if (errorRes.error?.messages?.size ?: 0 > 0) {
                    DottysBaseActivity().showSnackBarMessage(verificationActivity,
                        errorRes.error?.messages?.first() ?: ""
                    )
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
        changePassActivity.hideLoader()
        verificationCodeObserver = DottysForgotPasswordObserver(changePassActivity)
        newPasswordEditText  = changePassActivity.findViewById<EditText>(R.id.new_password_login_edittext)
        enterNewPasswordEditText  = changePassActivity.findViewById<EditText>(R.id.confirm_new_password_login_edittext)
        submitNewPasswordButton  = changePassActivity.findViewById<Button>(R.id.submit_enter_password_button)
        submitNewPasswordButton?.setOnClickListener {
            if (newPasswordEditText?.text.toString() != enterNewPasswordEditText?.text.toString()) {
                changePassActivity.showSnackBarMessage(changePassActivity,changePassActivity.getString(R.string.password_policy_check_message)
                )
            } else  if (!newPasswordEditText?.text.toString().isValidPassword()){
                changePassActivity.showSnackBarMessage(changePassActivity,"Password must match")
            } else {
                changePassword(changePassActivity, mail, newPasswordEditText?.text.toString(), code)
            }
        }
    }


    private fun changePassword(
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
                verificationActivity.hideLoader()
            },
            Response.ErrorListener { error ->
                verificationActivity.hideLoader()
                 if (error.networkResponse !=  null) {

                     val errorRes =
                         DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                     if (errorRes.error?.messages?.size ?: 0 > 0) {
                         DottysBaseActivity().showSnackBarMessage(verificationActivity,
                             errorRes.error?.messages?.first() ?: ""
                         )
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