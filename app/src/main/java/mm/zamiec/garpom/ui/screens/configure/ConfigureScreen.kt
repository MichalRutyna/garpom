package mm.zamiec.garpom.ui.screens.configure

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mm.zamiec.garpom.R
import mm.zamiec.garpom.ui.screens.configure.components.ServiceDiscoveryDataScreen

val TAG = "ConfigureScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("MissingPermission")
@Composable
fun ConfigureScreen(
    configureViewModel: ConfigureScreenViewModel = hiltViewModel(),
    onStationChosen: (String) -> Unit,
) {
    val activity = LocalActivity.current

    val configureState = configureViewModel.uiState.collectAsStateWithLifecycle()
    val scanResultsState = configureViewModel.scanResults

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            configureViewModel.onPermissionsGranted()
        } else {
            configureViewModel.onPermissionsDenied()
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
                configureViewModel.onBluetoothEnabled()
            }
        }
    }

    when (val s = configureState.value.screenState) {
        ScreenState.Initial -> {
            InitialScreen(
                onPair = { configureViewModel.initialConfiguration(activity!!) }
            )
        }
        ScreenState.Scanning -> {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Scanning...")
                LoadingIndicator()

            }
        }
        ScreenState.ScanResults -> {
            ScanResults(
                scanResultsState,
                { configureViewModel.initialConfiguration(activity!!) },
                onResultClicked = { onStationChosen(it) }
            )
        }
        is ScreenState.PairingError -> {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Pairing error: " + s.message)
            }
        }
        is ScreenState.ServiceDiscoveryData -> {
            ServiceDiscoveryDataScreen(s)
        }
    }

    when (configureState.value.dialog) {
        DialogState.DeviceIncompatible -> {
            DeviceIncompatibleDialog { configureViewModel.clearDialog() }
        }
        DialogState.PermissionsDenied -> {
            BluetoothRejectedDialog { configureViewModel.clearDialog() }
        }
        DialogState.PermissionExplanationNeeded -> {
            BluetoothExplanationDialog(
                onDismiss = { configureViewModel.onPermissionsDenied() },
                onConfirm = { configureViewModel.onExplanationAccepted() }
            )
        }
        null -> {}
    }

    LaunchedEffect(Unit) {
        configureViewModel.setRequestPermissionsCallback {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
        configureViewModel.setBluetoothEnableCallback {
            btEnableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }
}

@Composable
private fun InitialScreen(
    onPair: () -> Unit,
) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Configure")
        Button(onClick = onPair
        ) {
            Text("Scan")
        }
    }
}

@Composable
private fun ScanResults(
    scanResultsUiState: List<StationScanResult>,
    onRestart: () -> Unit,
    onResultClicked: (String) -> Unit,
) {
    if (scanResultsUiState.isEmpty()) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text("No devices found!", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(10.dp))
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = onRestart
                ) {
                    Text("Retry")
                }
            }
        }
    }
    else {
        LazyColumn {
            item {
                Text("Results:", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(10.dp))
            }
            items(scanResultsUiState) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Button(onClick = { onResultClicked(item.address) }) {
                        Text(item.address, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(10.dp))
                    }
                }
            }
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

@Preview(showBackground = true)
@Composable
private fun Preview() {
    val red = remember { TextFieldState() }
    val green = remember { TextFieldState() }
    val blue = remember { TextFieldState() }
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = {}) {
            Text("Discover services")
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            TextField(red, modifier = Modifier.weight(0.3f).padding(5.dp))
            TextField(green, modifier = Modifier.weight(0.3f).padding(5.dp))
            TextField(blue, modifier = Modifier.weight(0.3f).padding(5.dp))
        }
        Button(onClick = {}) {
            Text("Test light")
        }
    }
}