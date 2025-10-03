package mm.zamiec.garpom.ui.screens.configure

import android.Manifest
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.companion.AssociationInfo
import android.companion.CompanionDeviceManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mm.zamiec.garpom.R
import mm.zamiec.garpom.domain.model.state.ConfigureScreenState

val TAG = "ConfigureScreen"

@Composable
fun ConfigureScreen(
    bluetoothViewModel: BluetoothViewModel = hiltViewModel(),
    onUnableToConfigure: () -> Unit,
) {

    val context = LocalContext.current
    val activity = LocalActivity.current

    val configureState = bluetoothViewModel.uiState.collectAsStateWithLifecycle()

    val pairingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            RESULT_CANCELED -> {
                Log.d(TAG, "Pairing canceled")
            }
            RESULT_OK -> {
                val associationInfo : AssociationInfo? = result.data?.extras?.getParcelable(CompanionDeviceManager.EXTRA_ASSOCIATION,
                    AssociationInfo::class.java)
                Log.d(TAG, "Pairing successful ${associationInfo!!.displayName}")
            }
        }
    }

    val btPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Log.d(TAG, "Launcher granted permission")
            bluetoothViewModel.alertPermissionConfirmed()
        }
        else {
            Log.w(TAG, "Permission rejected")
            bluetoothViewModel.alertPermissionRejected()
        }
    }

    val btEnableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            RESULT_CANCELED -> {
                Log.d(TAG, "Bluetooth enable canceled")
            }
            RESULT_OK -> {
                bluetoothViewModel.alertPermissionConfirmed()
            }
        }
    }

    LifecycleResumeEffect(Unit) {
        Log.d(TAG, "RESUMED")
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
        bluetoothViewModel.updatePermissionStatus(hasPermission)
        onPauseOrDispose {  }
    }

    if (configureState.value == ConfigureScreenState.PermissionDialog) {
        BluetoothExplanationDialog(
            onDismiss = {
                bluetoothViewModel.alertPermissionRejected()
            },
            onConfirm = {
                btPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            })
    }

    if (configureState.value == ConfigureScreenState.BluetoothRejected) {
        BluetoothRejectedDialog(onConfirmed = {
            onUnableToConfigure()
            bluetoothViewModel.clearDialog()
        })
    }

    if (configureState.value == ConfigureScreenState.DeviceIncompatible) {
        DeviceIncompatibleDialog(onConfirmed = {
            onUnableToConfigure()
            bluetoothViewModel.clearDialog()
        })
    }

    if (configureState.value == ConfigureScreenState.PermissionConfirmed) {
        bluetoothViewModel.connectBluetooth(pairingLauncher)
        bluetoothViewModel.alertPairingLaunched()
    }

    Column {
        Text("Configure")
        Button(onClick = {
            bluetoothViewModel.pair(
                activity!!,
                btPermissionLauncher,
                btEnableLauncher,
                pairingLauncher
            )
        }) {
            Text("Pair")
        }
    }

}

@Composable
private fun BluetoothExplanationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog (
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.bt_perm_needed)) },
        text = { Text(stringResource(R.string.bt_perm_rationale)) },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
            }) { Text(stringResource(R.string.decline)) }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
            }) {
                Text("Proceed")
            }
        }
    )
}

@Composable
private fun BluetoothRejectedDialog(
    onConfirmed: () -> Unit,
){
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = {
//            onConfirmed
        },
        dismissButton = {
            TextButton(onClick = {
                onConfirmed()
            }) {
                Text("Ok")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(intent)
            }) {
                Text("Go to settings")
            }
        },
        title = {Text("Bluetooth permission rejected")},
        text = {Text("We need bluetooth to connect to your stations. You can change your permissions in the settings.")},
    )
}

@Composable
private fun DeviceIncompatibleDialog(
    onConfirmed: () -> Unit,
){
    AlertDialog(
        onDismissRequest = onConfirmed,
        confirmButton = {
            TextButton(onClick = onConfirmed) {
                Text("Ok")
            }
        },
        title = {Text("Device incompatible")},
        text = {Text("We weren't able to launch pairing from this device. It might mean it's incompatible. We apologize for the inconvenience.")},
    )
}