package mm.zamiec.garpom.ui.screens.station_config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.R
import mm.zamiec.garpom.ui.screens.station_config.components.ServiceDiscoveryDataScreen

@Composable
fun StationConfigScreen(
    address: String,
    stationConfigViewModel: StationConfigViewModel = hiltViewModel(
        creationCallback = { factory: StationConfigViewModel.Factory ->
            factory.create(address)
        }
    ),
    onBack: () -> Unit,
) {
    val state = stationConfigViewModel.uiState.collectAsState()
    val wifiList = stationConfigViewModel.wifiListState.collectAsState()
    val characteristicSwitch = stationConfigViewModel.characteristicSwitch.collectAsState()
    val discoveryData = stationConfigViewModel.serviceData.collectAsState()

    when (val s = state.value) {
        StationConfigUiState.Connecting -> {
            LoadingScreen()
        }

        is StationConfigUiState.WifiList -> {
            Column {
//                Row {
//                    Text("Show services data")
//                    Switch(
//                        characteristicSwitch.value,
//                        stationConfigViewModel::onCharacteristicSwitched
//                    )
//                }
                StationConfigContent(s,
                    wifiList.value,
                    onNetworkClicked = stationConfigViewModel::networkChosen,
                    onLightTestClicked = stationConfigViewModel::testLight,
                )
            }

        }

        is StationConfigUiState.ServiceDiscoveryData -> {
            Column {
                Row {
                    Text("Show services data")
                    Switch(
                        characteristicSwitch.value,
                        stationConfigViewModel::onCharacteristicSwitched
                    )
                }
                ServiceDiscoveryDataScreen(discoveryData.value)
            }
        }

        is StationConfigUiState.PasswordInput -> {
            LoadingScreen()
            PasswordDialog(
                s.selection,
                stationConfigViewModel::onPasswordDialogDismissed,
                stationConfigViewModel::onPasswordEntered
            )
        }

        is StationConfigUiState.Error -> {
            LaunchedEffect(Unit) {
                onBack()
            }
        }
    }
}


@Composable
private fun StationConfigContent(
    state: StationConfigUiState.WifiList,
    wifiList: List<WifiSelection>,
    onNetworkClicked: (WifiSelection) -> Unit,
    onLightTestClicked: (Int, Int, Int) -> Unit,
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
//        item {
//            LightTest(
//                onLightTestClicked
//            )
//        }
        item {
            Text("The station found the following networks:", textAlign = TextAlign.Center, modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp))
        }
        items(wifiList) { wifiNetwork ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Button(onClick = { onNetworkClicked(wifiNetwork) }) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (wifiNetwork.connected) {
                            Icon(
                                ImageVector.vectorResource(R.drawable.wifi_24px),
                                contentDescription = "Connected to this network"
                            )
                        } else {
                            Icon(
                                ImageVector.vectorResource(R.drawable.wifi_add_24px),
                                contentDescription = "Not connected"
                            )
                        }

                        Column (
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .weight(1f)
                        ) {
                            Text(wifiNetwork.name, textAlign = TextAlign.Left, modifier = Modifier.fillMaxWidth())
                            Text(wifiNetwork.ssid, textAlign = TextAlign.Left, modifier = Modifier.fillMaxWidth())
                        }
                        if (wifiNetwork.locked && !wifiNetwork.connected) {
                            Icon(Icons.Default.Lock,
                                contentDescription = "Requires password")
                        }
                    }
                }
            }
        }
    }

}


@Composable
private fun LightTest(
    onLightTestClicked: (Int, Int, Int) -> Unit,
) {
    val red = remember { TextFieldState("255") }
    val green = remember { TextFieldState("0") }
    val blue = remember { TextFieldState("0") }
    Column {

        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            TextField(red, modifier = Modifier
                .weight(0.3f)
                .padding(5.dp))
            TextField(green, modifier = Modifier
                .weight(0.3f)
                .padding(5.dp))
            TextField(blue, modifier = Modifier
                .weight(0.3f)
                .padding(5.dp))
        }
        Button(onClick = {
            onLightTestClicked(
                red.text.toString().toInt(),
                green.text.toString().toInt(),
                blue.text.toString().toInt(),
            )
        }) {
            Text("Test light")
        }

    }
}


@Composable
fun PasswordDialog(
    wifiSelection: WifiSelection,
    onDismissRequest: () -> Unit,
    onPasswordEntered: (WifiSelection, String) -> Unit,
) {
    val passwordState = rememberTextFieldState()

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Please enter the password for this network:",
                    modifier = Modifier
                        .padding(40.dp)
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center,
                )
                SecureTextField(
                    passwordState,
                    modifier = Modifier.fillMaxWidth()
                )
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                    Button(onClick = {onPasswordEntered(wifiSelection, passwordState.text.toString())},
                        modifier = Modifier.padding(horizontal = 5.dp)) {
                        Text("Ok")
                    }

                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingScreen() {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Pairing...")
        LoadingIndicator()

    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    val state = StationConfigUiState.WifiList
    val wifiList =
        listOf(
            WifiSelection(
                "Home network",
                "123",
                true,
                false,
            ),
            WifiSelection(
                "Work network",
                "123",
                false,
                false,
            )
    )
    StationConfigContent(
        state, wifiList, {}, {_, _, _ ->}
    )
//    PasswordDialog({}, {})
}