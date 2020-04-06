package com.keylimetie.dottys.register

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TextView
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.DottysLoginResponseModel
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R

class DottysRegisterActivity : DottysBaseActivity(), DatePickerDialog.OnDateSetListener,
    DottysRegisterUserDelegates {
    private val registerViewModel = DottysRegisterViewModel()
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
        hideCustomKeyboard()
    }

    override fun onResume() {
        super.onResume()
        print(this.currentFocus?.id)
//        val hide = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        hide.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        registerViewModel.birthdateEditText?.setText("$month / $dayOfMonth / $year")
    }

    override fun registerUser(userData: DottysLoginResponseModel) {
        //   Toast.makeText(this, "GO TO VERFY CODE", Toast.LENGTH_LONG).show()
        registerViewModel.showPreVerificationLayer(this)
    }

    override fun imageProfileHasUploaded(hasUploaded: Boolean) {
//        val intent = Intent(this, DottysMainNavigationActivity::class.java)
//        startActivity(intent)
    }
}
