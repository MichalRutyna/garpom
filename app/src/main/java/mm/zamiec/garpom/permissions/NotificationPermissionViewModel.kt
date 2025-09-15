package mm.zamiec.garpom.permissions

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.preference.PreferenceGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mm.zamiec.garpom.R

class NotificationPermissionViewModel(
    private val app: Application
) : AndroidViewModel(app) {
    private val _timesClicked = MutableStateFlow(0)

    private val _isPermissionGranted = MutableStateFlow(fetchPermissionsEnabled())
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    private val _areNotificationsEnabled = MutableStateFlow(fetchPermissionsEnabled())
    val areNotificationsEnabled: StateFlow<Boolean> = _areNotificationsEnabled.asStateFlow()

    fun shouldGoToSettings(): Boolean {
        _timesClicked.value += 1
        return _timesClicked.value >= 2
    }

    fun checkPermissions() {
        val granted = ContextCompat.checkSelfPermission(
            app,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        _isPermissionGranted.value = granted
        _areNotificationsEnabled.value = fetchPermissionsEnabled()
    }

    fun fetchPermissionsEnabled() : Boolean {
        return false
//        TODO fetch from datastore
    }

    fun updatePreference(granted: Boolean) {
        _areNotificationsEnabled.value = granted
        // TODO send to datastore
    }

    fun updatePermission(granted: Boolean) {
        _isPermissionGranted.value = granted
        _areNotificationsEnabled.value = granted
        // TODO send to datastore
    }


}