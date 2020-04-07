package com.keylimetie.dottys.register

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModel
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.keylimetie.dottys.DottysErrorModel
import com.keylimetie.dottys.DottysLoginResponseModel
import com.keylimetie.dottys.DottysRegisterRequestModel
import com.keylimetie.dottys.GpsTracker
import com.keylimetie.dottys.R.id
import com.keylimetie.dottys.forgot_password.DottysEnterVerificationCodeActivity
import com.keylimetie.dottys.register.volley_multipart.VolleyMultipartRequest
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt
import kotlin.properties.Delegates


open class DottysRegisterViewModel : ViewModel() {
    private var verificationLayout: ConstraintLayout? = null

    //region
    var activityRegisterObserver: DottysRegisterUserObserver? = null
    var activityRegister: DottysRegisterActivity? = null
    var editTextData: Array<EditText?>? = null
    var dataForRegister = DottysRegisterRequestModel()
    private var firstNameEditText: EditText? = null
    private var lastNameEditText: EditText? = null
    private var phoneEditText: EditText? = null
    private var emailEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var passwordConfirmEditText: EditText? = null
    var birthdateEditText: EditText? = null
    private var legalAgeCheckBox: CheckBox? = null
    private var termsAndConditionsCheckBox: CheckBox? = null
    private var showPasswordButon: Button? = null
    private var showConfirmPasswordButon: Button? = null
    private var showPasswordButonState = false
    private var showConfirmPasswordButonState = false
    private var submitRegisterButon: Button? = null
    private var phantonBirthdayButton: Button? = null
    //endregion

    fun initRegisterView(activityRegister: DottysRegisterActivity) {
        activityRegisterObserver = DottysRegisterUserObserver(activityRegister)
        initItemsAtView(activityRegister)
        addCustomsSettings()
        showPasswordButtonAction()
        submitRegisterButon?.setOnClickListener {
           // showPreVerificationLayer(activityRegister)/*FIXME*/
            editTextData?.let { it1 ->
                if (validateEditTextData(activityRegister, it1)) {
                    registerNewUser(activityRegister,dataForRegister)/*FIXME*/
                }
            }
        }
    }

    fun initItemsAtView(activityRegister: DottysRegisterActivity) {
        this.activityRegister = activityRegister
        activityRegister.hideLoader(activityRegister)
        firstNameEditText =
            activityRegister.findViewById<EditText>(id.first_name_register_edit_text)
        lastNameEditText =
            activityRegister.findViewById<EditText>(id.last_name_register_edit_text)
        phoneEditText = activityRegister.findViewById<EditText>(id.phone_register_edit_text)
        emailEditText = activityRegister.findViewById<EditText>(id.email_register_edit_text)
        passwordEditText = activityRegister.findViewById<EditText>(id.password_register_edit_text)
        passwordConfirmEditText =
            activityRegister.findViewById<EditText>(id.confirm_password_register_edit_text)
        birthdateEditText =
            activityRegister.findViewById<EditText>(id.birthdate_register_edit_text)
        legalAgeCheckBox = activityRegister.findViewById<CheckBox>(id.legal_age_register_checkbox)
        termsAndConditionsCheckBox =
            activityRegister.findViewById<CheckBox>(id.privacy_policy_register_checkbox)
        showPasswordButon = activityRegister.findViewById<Button>(id.show_password_button)
        showConfirmPasswordButon =
            activityRegister.findViewById<Button>(id.show_password_confirm_button)
        submitRegisterButon = activityRegister.findViewById<Button>(id.submit_register_button)
        phantonBirthdayButton = activityRegister.findViewById<Button>(id.phanton_birthdate_button)
        verificationLayout =
            activityRegister.findViewById<ConstraintLayout>(id.verification_layout)

        var paramsLayout = verificationLayout?.layoutParams
        paramsLayout?.height = 2
        verificationLayout?.layoutParams = paramsLayout
        initDatePackerView()
        activityRegister.hideCustomKeyboard()

    }


    fun showPasswordButtonAction() {
        showPasswordButon?.setOnClickListener {
            showPasswordButonState = !(showPasswordButonState)
            if (showPasswordButonState) {
                passwordEditText?.inputType = InputType.TYPE_CLASS_TEXT
            } else {
                passwordEditText?.inputType =
                    (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
            }
        }

        showConfirmPasswordButon?.setOnClickListener {
            showConfirmPasswordButonState = !(showConfirmPasswordButonState)
            if (showConfirmPasswordButonState) {
                passwordConfirmEditText?.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                passwordConfirmEditText?.inputType =
                    (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
            }
        }

    }

    fun initDatePackerView() {
        val c: Calendar = Calendar.getInstance()
        val year: Int = c.get(Calendar.YEAR)
        val month: Int = c.get(Calendar.MONTH)
        val day: Int = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = activityRegister?.let {
            DatePickerDialog(
                it, activityRegister, year - 18, month, day
            )
        }
//        birthdateEditText?.setOnFocusChangeListener { v, hasFocus ->
//            if (hasFocus) {
//                //    v.clearFocus()
//                activityRegister?.hideKeyboard()
//                datePickerDialog?.show()
//            }
//        }
        phantonBirthdayButton?.setOnClickListener { datePickerDialog?.show() }
    }

    fun addCustomsSettings() {
        editTextData = arrayOf(
            firstNameEditText,
            lastNameEditText,
            phoneEditText,
            emailEditText,
            passwordEditText,
            passwordConfirmEditText,
            birthdateEditText
        )
        for (item in editTextData!!) {
            when (item?.id) {
                id.phone_register_edit_text -> {
                    item.addTextChangedListener(object : PhoneNumberFormattingTextWatcher() {
                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            if (!item.text.contains("+1") && !item.text.isEmpty()) {
                                item.text.insert(0, "+1")
                            } else if (item.text.toString() == "+1") {
                                item.setText("")
                            }
                            super.onTextChanged(s, start, before, count)
                        }
                    })
                }
            }
        }
    }

    fun validateEditTextData(
        activityRegister: DottysRegisterActivity,
        editTextData: Array<EditText?>
    ): Boolean {
        for (item in editTextData) {
            if (item?.text?.isEmpty() == true) {
                Toast.makeText(activityRegister, "All field must be complete!", Toast.LENGTH_LONG)
                    .show()
                return false
            }
        }
        if (passwordEditText?.text.toString() != passwordConfirmEditText?.text.toString()) {
            Toast.makeText(activityRegister, "Password must match!", Toast.LENGTH_LONG).show()
            return false
        } else if (!activityRegister.isValidPassword(passwordEditText?.text.toString()) ||
            !activityRegister.isValidPassword(passwordConfirmEditText?.text.toString())
        ) {
            Toast.makeText(
                activityRegister,
                "Password must be at least 6 characters in length and contain 1 uppercase and 1 lowercase letter.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        if (legalAgeCheckBox?.isChecked != true) {
            Toast.makeText(activityRegister, "You must be of legal age", Toast.LENGTH_LONG).show()
            return false
        }
        if (termsAndConditionsCheckBox?.isChecked != true) {
            Toast.makeText(
                activityRegister,
                "You must accept the terms and conditions\n",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        //fillDataForRegisterRequest(editTextData)
        val gpsTracker = GpsTracker(activityRegister)
        val location = activityRegister.getLocation(gpsTracker, activityRegister)
        return true
    }

    fun fillDataForRegisterRequest(editTextData: Array<EditText?>) {
        for (item in editTextData) {
            when (item?.id) {
                id.first_name_register_edit_text -> {
                    dataForRegister.firstName = item.text.toString()
                }
                id.last_name_register_edit_text -> {
                    dataForRegister.lastName = item.text.toString()
                }
                id.phone_register_edit_text -> {
                    dataForRegister.cell =
                        item.text.toString().replace(" ", "").replace("(", "").replace(")", "")
                            .replace("-", "")
                }
                id.email_register_edit_text -> {
                    dataForRegister.email = item.text.toString()
                }
                id.password_register_edit_text -> {
                    dataForRegister.password = item.text.toString()
                }
                id.birthdate_register_edit_text -> {
                    dataForRegister.anniversaryDate = item.text.toString()
                }
                else -> {
                    return
                }
            }
        }
    }

    fun showPreVerificationLayer(activityRegister: DottysRegisterActivity) {
        //  val displayMetrics = DisplayMetrics()
        activityRegister.windowManager.defaultDisplay.getMetrics(activityRegister.displayMetrics)
        var imageVerification = activityRegister.findViewById<ImageView>(id.image_verification)
        var imageParams = imageVerification.layoutParams
        imageParams.height = (activityRegister.displayMetrics.heightPixels * 0.55).roundToInt()
        imageVerification.layoutParams = imageParams
        var actionBarParams = activityRegister.actionBarView?.layoutParams
        actionBarParams?.height = 0
        var paramsLayout = verificationLayout?.layoutParams
        paramsLayout?.height = activityRegister.displayMetrics.heightPixels
        ///verificationLayout.marginTop = 0
        verificationLayout?.layoutParams = paramsLayout
        activityRegister.actionBarView?.layoutParams = actionBarParams
        submitRegisterButon?.visibility = View.INVISIBLE
        activityRegister.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        activityRegister.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val goToVerification = activityRegister.findViewById<Button>(id.go_to_enter_verification)
        goToVerification.setOnClickListener {
            activityRegisterObserver?.registerUser?.email = "pruebaemail@mail.com"/*FIXME*/
            if (activityRegisterObserver?.registerUser?.email != null) {
                var intent =
                    Intent(activityRegister, DottysEnterVerificationCodeActivity::class.java)
                intent.putExtra("EMAIL_FORGOT", activityRegisterObserver?.registerUser?.email)
                intent.putExtra("REGISTER_VIEW_TYPE", true)
                intent.putExtra("USER_DATA", activityRegisterObserver?.registerUser?.toJson().toString())
                activityRegister.startActivity(intent)
            }
        }
    }

    /*REQUEST REGISTER*/
    fun registerNewUser(
        activityRegister: DottysRegisterActivity,
        registerData: DottysRegisterRequestModel
    ) {
        val mQueue = Volley.newRequestQueue(activityRegister)
        activityRegister.showLoader(activityRegister)

        val jsonObject = JSONObject(registerData.toJson())
        val jsonObjectRequest = object : JsonObjectRequest(Method.POST,
            activityRegister.baseUrl + "users/register",
            jsonObject,
            object : Response.Listener<JSONObject> {
                // activityRegister.hideLoader(activityRegister)
                override fun onResponse(response: JSONObject) {
                    activityRegister.hideLoader(activityRegister)

                    var user: DottysLoginResponseModel =
                        DottysLoginResponseModel.fromJson(
                            response.toString()
                        )

                    activityRegisterObserver?.registerUser = user
                }
            },
            Response.ErrorListener { error ->
                activityRegister.hideLoader(activityRegister)
                if (error.networkResponse != null) {
                    val errorRes = DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                    if (errorRes.error?.messages?.size ?: 0 > 0) {
                        Toast.makeText(activityRegister, errorRes.error?.messages?.first() ?: "", Toast.LENGTH_LONG).show()
                    }
                    Log.e("ERROR VOLLEY ", error.message, error)
                }
                // Log.e("TAG", error.message, error)
            }) { //no semicolon or coma

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                print(response?.statusCode)

                return super.parseNetworkResponse(response)
            }


        }
        mQueue.add(jsonObjectRequest)
    }

    fun uploadImgage(context: DottysProfilePictureActivity, imageData: ByteArray) {
        context.showLoader(context)
        val mQueue = Volley.newRequestQueue(context)
        val params = HashMap<String, String>()
        params["Authorization"] = context.getUserPreference().token ?: ""
        val request = object : VolleyMultipartRequest(
            context.baseUrl + "users/profilePicture",
            params,
            Response.Listener { response ->
                context.hideLoader(context)
                activityRegisterObserver?.imageHasUploaded = true
                print(response.statusCode)
            },
            Response.ErrorListener { error ->
                context.hideLoader(context)
                activityRegisterObserver?.imageHasUploaded = false
                val errorRes = DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                if (errorRes.error?.messages?.size ?: 0 > 0) {
                    Toast.makeText(context, errorRes.error?.messages?.first() ?: "", Toast.LENGTH_LONG).show()
                }
                Log.e("ERROR VOLLEY ", error.message, error)

            }) {

            override var byteData: Map<String, DataPart>?
                get() {
                    val params: MutableMap<String, DataPart> = HashMap()

                    params["profilePicture"] =
                        DataPart("profilePicture", imageData, "image/jpeg")

                    return params
                }
                set(value) {}
        }

        mQueue.add(request)
    }

}


/* REGISTER USER PROTOCOL */
//region
interface DottysRegisterUserDelegates {
    fun registerUser(userData: DottysLoginResponseModel)
    fun imageProfileHasUploaded(hasUploaded: Boolean)
}

class DottysRegisterUserObserver(lisener: DottysRegisterUserDelegates) {
    var registerUser: DottysLoginResponseModel by Delegates.observable(
        initialValue = DottysLoginResponseModel(),
        onChange = { _, _, new -> lisener.registerUser(new) })
    var imageHasUploaded: Boolean by Delegates.observable(
        initialValue = false,
        onChange = { _, _, new -> lisener.imageProfileHasUploaded(new) })

}
//endregion


