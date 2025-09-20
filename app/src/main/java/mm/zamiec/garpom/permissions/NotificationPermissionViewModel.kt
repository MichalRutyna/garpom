package mm.zamiec.garpom.permissions

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NotificationPermissionViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _timesClicked = MutableStateFlow(0)

    private val _isPermissionGranted = MutableStateFlow(fetchPreferenceEnabled())
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    private val _areNotificationsEnabled = MutableStateFlow(fetchPreferenceEnabled())
    val areNotificationsEnabled: StateFlow<Boolean> = _areNotificationsEnabled.asStateFlow()

    fun shouldGoToSettings(): Boolean {
        _timesClicked.value += 1
        return _timesClicked.value >= 2
    }

    fun checkPermissions() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        _isPermissionGranted.value = granted
        _areNotificationsEnabled.value = fetchPreferenceEnabled()
    }

    fun fetchPreferenceEnabled() : Boolean {
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