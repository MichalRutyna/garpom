package mm.zamiec.garpom.ui.screens.station

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.domain.model.state.StationScreenState
import mm.zamiec.garpom.ui.navigation.Station

@Composable
fun StationScreen(
    stationId: String,
    stationViewModel: StationViewModel = hiltViewModel(
        creationCallback = { factory: StationViewModel.Factory ->
            factory.create(stationId)
        }
    ),
    onMeasurementClicked: (String) -> Unit,
    onErrorClicked: (String) -> Unit,
) {
    val uiState: StationScreenState by stationViewModel.uiState.collectAsState(StationScreenState())

    StationScreenContent(uiState, onMeasurementClicked, onErrorClicked)
}

@Composable
private fun StationScreenContent(
    uiState: StationScreenState,
    onMeasurementClicked: (String) -> Unit,
    onErrorClicked: (String) -> Unit,
) {
    Text(uiState.name, style = MaterialTheme.typography.headlineLarge)
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    val uiState = StationScreenState(
        name = "Test station"
    )
    StationScreenContent(uiState, {}, {})
}