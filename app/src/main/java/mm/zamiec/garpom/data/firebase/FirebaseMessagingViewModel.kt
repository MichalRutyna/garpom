package mm.zamiec.garpom.data.firebase

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FirebaseMessagingViewModel @Inject constructor() : ViewModel() {

    private val TAG = "FirebaseMessaging"

    fun log_token(activity: Activity) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = "Hello $token"
            Log.d(TAG, msg)
        })
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(activity)
//    askNotificationPermission()
    }

    fun send() {

    }

    fun subscribe() {

        FirebaseMessaging.getInstance().subscribeToTopic("test")
//    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
//        if (!task.isSuccessful) {
//            Log.w(TAG, "Fetching FCM registration token failed", task.exception)
//            return@OnCompleteListener
//        }
//
//        // Get new FCM registration token
//        val token = task.result
//
//        // Log and toast
//        val msg = getString(R.string.msg_token_fmt, token)
//        Log.d(TAG, msg)
//        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//    })
//
//    FirebaseMessaging.getInstance().subscribeToTopic("test")
//    GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
//    askNotificationPermission()
    }
}