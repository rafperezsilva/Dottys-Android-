package com.keylimetie.dottys.register

import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModel
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.keylimetie.dottys.*
import com.keylimetie.dottys.R.id
import com.keylimetie.dottys.TermsAndPrivacy.TermsAndPrivacyActivity
import com.keylimetie.dottys.forgot_password.DottysEnterVerificationCodeActivity
import com.keylimetie.dottys.login.DottysLoginDelegate
import com.keylimetie.dottys.login.DottysLoginObserver
import com.keylimetie.dottys.login.DottysLoginViewModel
import com.keylimetie.dottys.ui.locations.showSnackBarMessage
import com.keylimetie.dottys.utils.isValidPassword
import com.keylimetie.dottys.utils.volley_multipart.VolleyMultipartRequest
import org.json.JSONObject
import java.util.*
import kotlin.math.roundToInt
import kotlin.properties.Delegates


open class DottysRegisterViewModel(val dottysBaseActivity: DottysBaseActivity): ViewModel(), View.OnClickListener, DottysLoginDelegate {
    private var termsOfServiceLabel: TextView? = null
    private var privacyPolicyLabel: TextView? = null
    private var verificationLayout: ConstraintLayout? = null
    private var loginFloatingLayout: ConstraintLayout? = null

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
    private var emailEditTextFloating   : EditText? = null
    private var passwordEditTextFloating: EditText? = null
    //private var passwordConfirmEditText: EditText? = null
    var birthdateEditText: EditText? = null
    private var legalAgeCheckBox: CheckBox? = null
    private var termsAndConditionsCheckBox: CheckBox? = null
    private var showPasswordButon: Button? = null
   // private var showConfirmPasswordButon: Button? = null
    private var showPasswordButonState = false
   // private var showConfirmPasswordButonState = false
    private var submitRegisterButon: Button? = null
    private var phantonBirthdayButton: Button? = null
    var isRegisterUser = false
    //endregion
    var heigth = 0f
    fun initRegisterView(activityRegister: DottysRegisterActivity) {
        activityRegisterObserver = DottysRegisterUserObserver(activityRegister)
        isRegisterUser = activityRegister.intent.getBooleanExtra("IS_REGISTER_USER",false)
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

   private fun initItemsAtView(activityRegister: DottysRegisterActivity) {
        this.activityRegister = activityRegister
        activityRegister.hideLoader()
        firstNameEditText =
            activityRegister.findViewById<EditText>(id.first_name_register_edit_text)
        lastNameEditText =
            activityRegister.findViewById<EditText>(id.last_name_register_edit_text)
        phoneEditText = activityRegister.findViewById(id.phone_register_edit_text)
        emailEditText = activityRegister.findViewById<EditText>(id.email_register_edit_text)
        passwordEditText = activityRegister.findViewById<EditText>(id.password_register_edit_text)
        passwordEditText = activityRegister.findViewById<EditText>(id.password_register_edit_text)
        termsOfServiceLabel = activityRegister.findViewById<TextView>(id.terms_of_service_text_view)
        privacyPolicyLabel = activityRegister.findViewById<TextView>(id.privacy_policy_text_view)
        val singFromRegister = activityRegister.findViewById<TextView>(id.sign_from_register)
        birthdateEditText =
            activityRegister.findViewById<EditText>(id.birthdate_register_edit_text)
        legalAgeCheckBox = activityRegister.findViewById<CheckBox>(id.legal_age_register_checkbox)
        termsAndConditionsCheckBox =
            activityRegister.findViewById<CheckBox>(id.privacy_policy_register_checkbox)
        showPasswordButon = activityRegister.findViewById<Button>(id.show_password_button)
        submitRegisterButon = activityRegister.findViewById<Button>(id.submit_register_button)
        phantonBirthdayButton = activityRegister.findViewById<Button>(id.phanton_birthdate_button)
        verificationLayout =
            activityRegister.findViewById<ConstraintLayout>(id.verification_constraint_layout)
          loginFloatingLayout =
            activityRegister.findViewById<ConstraintLayout>(id.login_floating_view)
        heigth = activityRegister?.resources?.displayMetrics?.heightPixels?.toFloat() ?: 0.0f
       loginFloatingLayout?.animate()?.translationY(-heigth)?.setDuration(0)?.start()
        var paramsLayout = verificationLayout?.layoutParams
        paramsLayout?.height = 2
        verificationLayout?.layoutParams = paramsLayout
        initDatePackerView()
        activityRegister.hideCustomKeyboard(activityRegister)
       loginFloatingLayout?.setOnClickListener(this)
       termsOfServiceLabel?.setOnClickListener(this)
       privacyPolicyLabel?.setOnClickListener(this)
       singFromRegister?.setOnClickListener(this)
       val scrollView = activityRegister.findViewById<ScrollView>(id.register_scroll_view)
       scrollView.alpha = if(isRegisterUser) 0.0f else 1.0f
     }

    private fun initLoginFloatinItems(){
         emailEditTextFloating    = activityRegister?.findViewById<EditText>(R.id.email_floating_edit_text)
         passwordEditTextFloating = activityRegister?.findViewById<EditText>(R.id.password_floating_edit_text)
        val submitFloatingButton = activityRegister?.findViewById<Button>(R.id.submit_floating_button)
        val cancelFloatingButton = activityRegister?.findViewById<Button>(R.id.cancel_floating_button)
        submitFloatingButton?.setOnClickListener(this)
        cancelFloatingButton?.setOnClickListener(this)
    }


    private fun showPasswordButtonAction() {
        showPasswordButon?.setOnClickListener {
            showPasswordButonState = !(showPasswordButonState)
            if (showPasswordButonState) {
                passwordEditText?.inputType = InputType.TYPE_CLASS_TEXT
            } else {
                passwordEditText?.inputType =
                    (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
            }
        }

//        showConfirmPasswordButon?.setOnClickListener {
//            showConfirmPasswordButonState = !(showConfirmPasswordButonState)
//            if (showConfirmPasswordButonState) {
//                passwordConfirmEditText?.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
//            } else {
//                passwordConfirmEditText?.inputType =
//                    (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
//            }
//        }

    }

    private fun initDatePackerView() {
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
        phantonBirthdayButton?.setOnClickListener {
            activityRegister?.hideCustomKeyboard(activityRegister ?: return@setOnClickListener)
            datePickerDialog?.show() }
    }

    private fun addCustomsSettings() {
        editTextData = arrayOf(
            firstNameEditText,
            lastNameEditText,
            phoneEditText,
            emailEditText,
            passwordEditText,
            //passwordConfirmEditText,
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

    private fun validateEditTextData(
        activityRegister: DottysRegisterActivity,
        editTextData: Array<EditText?>
    ): Boolean {
        for (item in editTextData) {
            if (item?.text?.isEmpty() == true) {
                activityRegister.showSnackBarMessage(activityRegister,
                    activityRegister.getString(R.string.complete_all_fields)
                )
                return false
            }
        }
        if (!passwordEditText?.text.toString().isValidPassword()) {
            activityRegister.showSnackBarMessage(activityRegister,
                activityRegister.getString(R.string.password_policy_check_message)
            )
            return false
        }
        if (legalAgeCheckBox?.isChecked != true) {
            activityRegister.showSnackBarMessage(activityRegister,
                activityRegister.getString(R.string.legal_age_message)
            )
            return false
        }
        if (termsAndConditionsCheckBox?.isChecked != true) {
            activityRegister.showSnackBarMessage(activityRegister,
                activityRegister.getString(R.string.terms_and_conditions_messages)
            )
            return false
        }
        fillDataForRegisterRequest(editTextData)
       // val gpsTracker = GpsTracker(activityRegister)
        /*GET DEVICE LOCATION ON REGISTER*/
      //  val location = activityRegister.getLocation(gpsTracker, activityRegister)
        return true
    }

    private fun fillDataForRegisterRequest(editTextData: Array<EditText?>) {
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
        val imageVerification = activityRegister.findViewById<ImageView>(id.image_verification)
        val subTitlePreverificationLabel = activityRegister.findViewById<TextView>(id.subtitle_register_sucess_textview)
        val imageParams = imageVerification.layoutParams
        imageParams.height = (activityRegister.displayMetrics.heightPixels * 0.55).roundToInt()
        imageVerification.layoutParams = imageParams
        val actionBarParams = activityRegister.actionBarView?.layoutParams
        actionBarParams?.height = 0
        val paramsLayout = verificationLayout?.layoutParams
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
        var subtitleMssg =  if(activityRegister.getUserPreference().cell.isNullOrEmpty()){
            "A text message with a verification\ncode has been sent to phone"
        } else {
            val cell = activityRegister.getUserPreference().cell ?: ""
            val terminalPhoneNumber =   cell?.subSequence(cell?.count()  - 4, cell.count())
            "A text message with a verification\ncode has been sent to (xxx) xxx-${terminalPhoneNumber}."
        }

        subTitlePreverificationLabel.text = subtitleMssg
        val goToVerification = activityRegister.findViewById<Button>(R.id.go_to_enter_verification)
        goToVerification.setOnClickListener {
           val currentUser = activityRegisterObserver?.registerUser
            if (currentUser != null) {
                var intent =
                    Intent(activityRegister, DottysEnterVerificationCodeActivity::class.java)
                intent.putExtra("EMAIL_FORGOT", currentUser.email ?: dottysBaseActivity.getUserPreference().email)
                if (dottysBaseActivity.getUserPreference() !=  null &&
                    dottysBaseActivity.getUserPreference().cellVerified == false &&
                        isRegisterUser) {
                    intent.putExtra("VERIFY_CELL", true)
                }
                intent.putExtra("REGISTER_VIEW_TYPE", true)
                intent.putExtra("USER_DATA", activityRegisterObserver?.registerUser?.toJson().toString())
                activityRegister.startActivity(intent)




            }
        }
    }

    /**
     * REQUEST REGISTER
     * */
    private fun registerNewUser(
        activityRegister: DottysRegisterActivity,
        registerData: DottysRegisterRequestModel
    ) {
        val mQueue = Volley.newRequestQueue(activityRegister)
        activityRegister.showLoader()

        val jsonObject = JSONObject(registerData.toJson())
        val jsonObjectRequest = object : JsonObjectRequest(Method.POST,
            activityRegister.baseUrl + "users/register",
            jsonObject,
            Response.Listener<JSONObject> { response ->

                // activityRegister.hideLoader(activityRegister)
                activityRegister.hideLoader()

                var user: DottysLoginResponseModel =
                    DottysLoginResponseModel.fromJson(
                        response.toString()
                    )

                activityRegisterObserver?.registerUser = user
            },
            Response.ErrorListener { error ->
                activityRegister.hideLoader()
                if (error.networkResponse != null) {
                    activityRegister.hideCustomKeyboard(activityRegister)
                    val errorRes = DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                    if (errorRes.error?.messages?.size ?: 0 > 0) {
                        DottysBaseActivity().showSnackBarMessage(activityRegister,
                            errorRes.error?.messages?.first() ?: ""
                        )
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
/**
 * UPLOAD IMAGE
 * */
    fun   uploadProfileImage(context: DottysBaseActivity, imageData: ByteArray) {
      context.showLoader()

        val mQueue = Volley.newRequestQueue(context)
        val params = HashMap<String, String>()
        params["Authorization"] = context.getUserPreference().token ?: ""
        val request = object : VolleyMultipartRequest(
            context.baseUrl + "users/profilePicture",
            params,
            Response.Listener { response ->
                 context.hideLoader()
                activityRegisterObserver?.imageHasUploaded = true
                print(response.statusCode)
            },
            Response.ErrorListener { error ->
               context.hideLoader()
                activityRegisterObserver?.imageHasUploaded = false
                if(error.networkResponse != null ) {
                    val errorRes =
                        DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                    if (errorRes.error?.messages?.size ?: 0 > 0) {
                        DottysBaseActivity().showSnackBarMessage(context,
                            errorRes.error?.messages?.first() ?: ""
                        )
                    }
                    Log.e("ERROR VOLLEY ", error.message, error)
                }

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

    /**
     * REQUEST VERIFICATION PHONE
     * */
    fun requestNewVerificationPhone (context: DottysBaseActivity) {
      context.showLoader()
        val mQueue = Volley.newRequestQueue(context)
        val params = HashMap<String, String>()
        params["Authorization"] = context.getUserPreference().token ?: ""
        val request = object : VolleyMultipartRequest(
            context.baseUrl + "users/requestVerifyPhone",
            params,
            Response.Listener { response ->
                 context.hideLoader()
                val container = context.findViewById<View>(android.R.id.content)
                if (container != null) {
                    context.hideCustomKeyboard(context)
                    Snackbar.make(container, "A verification code has been send", Snackbar.LENGTH_LONG).show()
                }
                Log.i("REQUEST VERIFY PHONE","STATUS ${response.statusCode}")
            },
            Response.ErrorListener { error ->
               context.hideLoader()
                context.finish()
                 if(error.networkResponse != null ) {
                    val errorRes =
                        DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                    if (errorRes.error?.messages?.size ?: 0 > 0) {
                        DottysBaseActivity().showSnackBarMessage(context,
                            errorRes.error?.messages?.first() ?: ""
                        )
                    }
                    Log.e("ERROR VOLLEY ", error.message, error)
                }

            }) {


        }

        mQueue.add(request)
    }

   private fun loginFromFloatingView(){
        val loginViewModel = DottysLoginViewModel()
        val registerModel = DottysRegisterModel()
       loginViewModel.userLoginDataObserver = DottysLoginObserver(this)
       registerModel.email = emailEditTextFloating?.text.toString()
       registerModel.password = passwordEditTextFloating?.text.toString()
       loginViewModel.loginUserRequest(registerModel, activityRegister ?: DottysBaseActivity())
    }


    override fun onClick(v: View?) {
        val i = Intent(activityRegister,TermsAndPrivacyActivity::class.java)
        when(v?.id){
            R.id.terms_of_service_text_view -> {
                i.putExtra("TERMS_PRIVACY","TERMS")
                activityRegister?.startActivity(i)
            }
            R.id.privacy_policy_text_view -> {
                i.putExtra("TERMS_PRIVACY","PRIVACY")
                activityRegister?.startActivity(i)
            }
            R.id.sign_from_register -> {
                initLoginFloatinItems()
                loginFloatingLayout?.animate()?.translationY(0f)?.setDuration(450)?.start()
            }
            R.id.login_floating_view, R.id.cancel_floating_button -> {
                loginFloatingLayout?.animate()?.translationY(-heigth)?.setDuration(450)?.start()
            }
            R.id.submit_floating_button -> {
                loginFromFloatingView()
            }
        }
    }

    override fun onUserLogin(registerUserData: DottysLoginResponseModel) {
        activityRegister?.saveDataPreference(PreferenceTypeKey.USER_DATA, registerUserData.toJson())
        val intent = Intent(activityRegister, DottysMainNavigationActivity::class.java)
        activityRegister?.startActivity(intent)

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



