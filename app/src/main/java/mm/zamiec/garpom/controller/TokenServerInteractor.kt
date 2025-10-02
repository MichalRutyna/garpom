package mm.zamiec.garpom.controller

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.messaging
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


@ActivityRetainedScoped
class TokenServerInteractor @Inject constructor(private val firebaseAuth: FirebaseAuth) {
    private val TAG = "TokenServerInteractor"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
        val deviceToken = hashMapOf(
            "token" to token,
            "timestamp" to FieldValue.serverTimestamp(),
        )
        if (firebaseAuth.currentUser == null) {
            Log.e(TAG, "Send token called, but no user in auth")
            return
        }
        val userId = firebaseAuth.currentUser!!.uid
        Firebase.firestore.collection("fcmTokens").document(userId)
            .set(deviceToken)
    }

    fun ensureToken() {
        serviceScope.launch {
            val token = Firebase.messaging.token.await()
            sendRegistrationToServer(token)
        }
    }
}