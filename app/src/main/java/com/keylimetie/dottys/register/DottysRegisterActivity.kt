package com.keylimetie.dottys.register

import android.app.DatePickerDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.DottysLoginResponseModel
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
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        registerViewModel.birthdateEditText?.setText("$month / $dayOfMonth / $year")
    }

    override fun registerUser(userData: DottysLoginResponseModel) {
        Toast.makeText(this, "GO TO VERFY CODE", Toast.LENGTH_LONG).show()
        registerViewModel.showPreVerificationLayer(this)
    }
}
