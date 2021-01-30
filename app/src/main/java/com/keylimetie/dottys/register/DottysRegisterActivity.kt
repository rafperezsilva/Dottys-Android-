package com.keylimetie.dottys.register

import android.app.DatePickerDialog
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TextView
import com.keylimetie.dottys.*
import com.keylimetie.dottys.ui.locations.showSnackBarMessage
import java.time.LocalDate
import java.util.*

class DottysRegisterActivity : DottysBaseActivity(), DatePickerDialog.OnDateSetListener,
    DottysRegisterUserDelegates {
    private val registerViewModel = DottysRegisterViewModel(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_register)
        this.supportActionBar?.let {
            actionBarSetting(
                it,
                ColorDrawable(resources.getColor(R.color.colorPrimary))
            )
        }
        val titleBar = actionBarView!!.findViewById<TextView>(R.id.title_bar)
        titleBar.text = "Create Account"

        registerViewModel.initRegisterView(this)
        hideCustomKeyboard(this)
        setBackButton()
        backButton?.setImageResource(R.drawable.close_icon)
        if(registerViewModel.isRegisterUser){
            hideCustomKeyboard(this)
            registerViewModel.showPreVerificationLayer(this)
            registerViewModel.requestNewVerificationPhone(this)
        }

    }

    override fun onResume() {
        super.onResume()
        print(this.currentFocus?.id)
//        val hide = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        hide.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val c: Calendar = Calendar.getInstance()
        val dateSelected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.of(year,month + 1,dayOfMonth)
        } else {
            return
        }
        val currentDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.of(c.get(Calendar.YEAR) - 18,c.get(Calendar.MONTH) + 1,c.get(Calendar.DAY_OF_MONTH))
        } else {
            return
        }
        if (dateSelected.isAfter(currentDate)) {
            registerViewModel.birthdateEditText?.setText("")
            DottysBaseActivity().showSnackBarMessage(this,
                "You must be 21 years old to participate"
            )
        } else {
            registerViewModel.birthdateEditText?.setText("${month + 1} / $dayOfMonth / $year")
        }
    }

    override fun registerUser(userData: DottysLoginResponseModel) {
        //   DottysBaseActivity().showSnackBarMessage(this, "GO TO VERFY CODE")
        hideCustomKeyboard(this)
        saveDataPreference(PreferenceTypeKey.USER_DATA,userData.toJson())
        registerViewModel.showPreVerificationLayer(this)
    }

    override fun onBackPressed() {
        if (!registerViewModel.isRegisterUser)
        super.onBackPressed()
    }

    override fun imageProfileHasUploaded(hasUploaded: Boolean) {
//        val intent = Intent(this, DottysMainNavigationActivity::class.java)
//        startActivity(intent)
    }
}
