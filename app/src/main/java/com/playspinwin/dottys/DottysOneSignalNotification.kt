package com.playspinwin.dottys

import android.util.Log
import com.onesignal.OSNotification
import com.onesignal.OSNotificationAction.ActionType
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal.NotificationOpenedHandler
import com.onesignal.OneSignal.NotificationReceivedHandler


open class NotificationOneSignaldHandler : NotificationReceivedHandler, NotificationOpenedHandler {
    override fun notificationReceived(notification: OSNotification) {
        val data = notification.payload.additionalData
        val customKey: String?
        if (data != null) {
            customKey = data.optString("customkey", null)
            if (customKey != null) Log.i(
                "OneSignalExample",
                "customkey set with value: $customKey"
            )
        }
    }

    // This fires when a notification is opened by tapping on it.
    override fun notificationOpened(result: OSNotificationOpenResult) {
        val actionType = result.action.type
        val data = result.notification.payload.additionalData
        val customKey: String?
        Log.i(
            "OSNotificationPayload",
            "result.notification.payload.toJSONObject().toString(): " + result.notification.payload.toJSONObject()
                .toString()
        )
        if (data != null) {
            customKey = data.optString("customkey", null)
            if (customKey != null) Log.i(
                "OneSignalExample",
                "customkey set with value: $customKey"
            )
        }
        if (actionType == ActionType.ActionTaken) Log.i(
            "OneSignalExample",
            "Button pressed with id: " + result.action.actionID
        )

        // The following can be used to open an Activity of your choice.
        // Replace - getApplicationContext() - with any Android Context.
        // Intent intent = new Intent(getApplicationContext(), YourActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        // startActivity(intent);

        // Add the following to your AndroidManifest.xml to prevent the launching of your main Activity
        //   if you are calling startActivity above.
        /*
        <application ...>
          <meta-data android:name="com.onesignal.NotificationOpened.DEFAULT" android:value="DISABLE" />
        </application>
     */
    }
}