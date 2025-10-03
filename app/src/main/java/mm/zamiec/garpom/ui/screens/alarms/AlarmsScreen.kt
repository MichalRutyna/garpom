package mm.zamiec.garpom.ui.screens.alarms

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import mm.zamiec.garpom.controller.firebase.FirebaseMessagingViewModel
import mm.zamiec.garpom.controller.firebase.MyFirebaseMessagingService
import mm.zamiec.garpom.domain.usecase.NotificationPermissionViewModel


@Composable
fun AlarmsScreen(
    notificationViewModel: NotificationPermissionViewModel = hiltViewModel(),
    firebaseMessagingViewModel: FirebaseMessagingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    val notificationPermissionGranted by notificationViewModel.isPermissionGranted.collectAsState()
    val notificationPreferenceEnabled by notificationViewModel.areNotificationsEnabled.collectAsState()

    Column {
        Text("Alarms")
        Button(
            onClick = {
                firebaseMessagingViewModel.log_token(activity!!)
            }
        ) {
            Text("Show token")
        }
        Button(
            onClick = {
            }
        ) {
            Text("Send token")
        }
        LifecycleResumeEffect(Unit) {
            notificationViewModel.checkPermissions()
            onPauseOrDispose { }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Push notifications: ")
            Switch(
                checked = notificationPreferenceEnabled,
                onCheckedChange = { isChecked ->
                    notificationViewModel.checkPermissions()
                    if (isChecked) {
                        if (notificationPermissionGranted) {
                            notificationViewModel.updatePreference(true)
                        } else {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    } else {
                        notificationViewModel.updatePreference(false)
                    }
                }
            )
        }

        Button(onClick = {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
        }) {
            Text("Go to settings")
        }
        Text("Permission: $notificationPermissionGranted")
        Text("Preference: $notificationPreferenceEnabled")
    }
}