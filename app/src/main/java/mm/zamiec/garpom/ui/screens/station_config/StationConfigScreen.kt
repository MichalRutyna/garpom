package mm.zamiec.garpom.ui.screens.station_config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun StationConfigScreen(
    address: String,
    stationConfigViewModel: StationConfigViewModel = hiltViewModel(
        creationCallback = { factory: StationConfigViewModel.Factory ->
            factory.create(address)
        }
    ),
) {
    val state = stationConfigViewModel.uiState.collectAsState()
    StationConfigContent(state.value)
}


@Composable
private fun StationConfigContent(
    state: StationConfigUiState
) {
    val red = remember { TextFieldState() }
    val green = remember { TextFieldState() }
    val blue = remember { TextFieldState() }
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(state.address)
//        Button(onClick = { configureViewModel.discoverServices(s.station) }) {
//            Text("Discover services")
//        }
//        Row(horizontalArrangement = Arrangement.SpaceBetween) {
//            TextField(red, modifier = Modifier.weight(0.3f).padding(5.dp))
//            TextField(green, modifier = Modifier.weight(0.3f).padding(5.dp))
//            TextField(blue, modifier = Modifier.weight(0.3f).padding(5.dp))
//        }
//        Button(onClick = { configureViewModel.testLight(
//            s.station,
//            red.text.toString().toInt(),
//            green.text.toString().toInt(),
//            blue.text.toString().toInt(),
//        ) }) {
//            Text("Test light")
//        }
    }
}


@Preview(showBackground = true)
@Composable
private fun Preview() {
    val state = StationConfigUiState(
        "test"
    )
    StationConfigContent(
        state
    )
}