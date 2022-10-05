package com.keylimetie.dottys

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import androidx.core.app.JobIntentService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.keylimetie.dottys.forgot_password.DottysEnterVerificationCodeActivity
import com.keylimetie.dottys.register.DottysRegisterActivity
import com.keylimetie.dottys.ui.drawing.models.DottysDrawingRewardsModel
import com.keylimetie.dottys.ui.drawing.models.DottysDrawingUserModel
import com.keylimetie.dottys.ui.locations.showSnackBarMessage
import com.keylimetie.dottys.utils.geofence.GeofenceTransitionsJobIntentService
import org.altbeacon.beacon.Beacon
import java.util.*
import kotlin.properties.Delegates





class SMSTransitionsJobIntentService: JobIntentService() {
    val TAG = "SMS JOB SERVICE"
    override fun onHandleWork(intent: Intent) {
        Log.i(TAG, "Intent HandleWork: " + intent.action)
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, "Intent onBind: " + intent.action)
        return super.onBind(intent)
    }

    companion object {
        private const val JOB_ID = 1000
        private const val TAG = "SMSTransitionsIS"
        const val CHANNEL_ID = "channel_03"

        /**
         * Convenience method for enqueuing work in to this service.
         */
        fun enqueueWork(context: Context?, intent: Intent?) {
            enqueueWork(context!!,
                SMSTransitionsJobIntentService::class.java,
                JOB_ID,
                intent!!)
        }
    }
}

class SmsListener(
    private val activity: DottysBaseActivity

)
        : BroadcastReceiver() {

    private val SMS_RECEIVED: String = "android.provider.Telephony.SMS_RECEIVED"
    private val TAG: String = "SmsBroadcastReceiver"
    var msg: String = ""
    var phoneNo: String = ""
    var smsData = SMSReceiveData()
    override fun peekService(myContext: Context?, service: Intent?): IBinder {
        myContext?.let { service?.let { it1 -> actionReceiveHandler(it, it1) } }
        return super.peekService(myContext, service)
    }
    override fun onReceive(context: Context, intent: Intent) {
         actionReceiveHandler(context,intent)
    }

    private fun actionReceiveHandler(context: Context, intent: Intent){
        Log.i(TAG, "Intent Received: " + intent.action)
        if (intent.action == SMS_RECEIVED){

            //retrieves a map of extended data from the intent
            var dataBundle: Bundle? = intent.extras
            if (dataBundle != null){

                //creating PDU(Protocol Data Unity) object witch is a protocol for transferring message
                var mypdu = dataBundle.get("pdus") as Array<Any>
                //arrayOfNulls???
                val message = arrayOfNulls<SmsMessage>(mypdu.size)

                for (i in 0 until mypdu.size) {

                    //for build versions >= API Level 23
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        var format: String? = dataBundle.getString("format")

                        //From PDU we get all object and SmsMessage Objet using following line of code
                        message[i] = SmsMessage.createFromPdu(mypdu[i] as ByteArray, format)
                    } else {

                        //<API Level 23
                        message[i] = SmsMessage.createFromPdu(mypdu[i] as ByteArray)
                    }
                    msg = message[i]!!.messageBody
                    phoneNo = message[i]!!.originatingAddress!!
                }
                Log.d("🫔SMS","-- MENSAJE  RECIVFO $msg")
                //  SMSReceiverObserver(context as DottysBaseActivity).sms = msg
                val startIntent = Intent(context, DottysEnterVerificationCodeActivity::class.java)//context
                //   .packageManager
                // .getLaunchIntentForPackage("com.keylimetie.dottys")
//
                (activity as DottysEnterVerificationCodeActivity).forgotViewModel.fillCodeVerification(msg)
                startIntent.putExtra("SMS_CODE",msg)
                startIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
//                (context  as DottysBaseActivity).showSnackBarMessage(context,"HELLO MYFRIEND")
//                SMSTransitionsJobIntentService.Companion.enqueueWork(context, startIntent)
//                 startIntent?.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                smsData.sms.postValue(msg)

//                context.startActivity(startIntent)

//                Toast.makeText(context, "Message: " + msg + "\nNember: " +phoneNo, Toast.LENGTH_LONG).show()
            }
        }
    }


    // This MutableLiveData mechanism is used for sharing centralized beacon data with the ViewControllers
    class SMSReceiveData: ViewModel() {
        val sms: MutableLiveData<String> by lazy {
            //observer.sms = sms.toString()
            MutableLiveData<String>()

        }
    }
}


interface SMSReceiverDelegates {
    fun onSmsRetrived(sms: String)


}

class SMSReceiverObserver(lisener: SMSReceiverDelegates) {
    var sms: String by Delegates.observable(
        initialValue = "",
        onChange = { _, _, new -> lisener.onSmsRetrived(new) })

}
    /**BroadcastReceiver() {
    private val preferences: SharedPreferences? = null
    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras //---get the SMS message passed in---
            var msgs: Array<SmsMessage?>? = null
            //var msg_from = String()
            if (bundle != null) {
                //---retrieve the SMS message received---
                try {
                    val pdus = bundle["pdus"] as Array<Any>?
                    msgs = arrayOfNulls<SmsMessage>(pdus!!.size)
                    for (i in msgs.indices) {
                        msgs!![i] = SmsMessage.createFromPdu(pdus!![i] as ByteArray)
                        val  msg_from = msgs[i]?.originatingAddress.toString()
                        val msgBody: String = msgs[i]?.messageBody ?: "EMPTY"
                    }
                } catch (e: Exception) {
                            Log.e("Exception SMS caught", "${e.message}")
                }
            }
        }
    }
}

fun DottysBaseActivity.getAllSms(): List<Sms>? {
    var lstSms: ArrayList<Sms> = ArrayList()
    var objSms = Sms()
    val message: Uri = Uri.parse("content://sms/")
    val cr: ContentResolver = this.getContentResolver()
    val c: Cursor? = cr.query(message, null, null, null, null)
    this.startManagingCursor(c)
    val totalSMS: Int? = c?.getCount()
    if (c?.moveToFirst() == true) {
        for (i in 0 until (totalSMS ?: 0)) {
            objSms = Sms()
            objSms.id = (c.getString(c.getColumnIndexOrThrow("_id")))
            objSms.address = (
                    c.getString(
                        c
                            .getColumnIndexOrThrow("address")
                    )
            )
            objSms.msg = (c.getString(c.getColumnIndexOrThrow("body")))
            objSms.readState =  (c.getString(c.getColumnIndex("read")))
            objSms.time = (c.getString(c.getColumnIndexOrThrow("date")))
            if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                objSms.folderName = ("inbox")
            } else {
                objSms.folderName = ("sent")
            }
            lstSms.add(objSms)
            c.moveToNext()
        }
    }
    // else {
    // throw new RuntimeException("You have no SMS");
    // }
    c?.close()
    return lstSms
}


class Sms {
    var id: String? = null
    var address: String? = null
    var msg: String? = null
    var readState //"0" for have not read sms and "1" for have read sms
            : String? = null
    var time: String? = null
    var folderName: String? = null
}
     */