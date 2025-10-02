package mm.zamiec.garpom.ui.screens.station

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.ui.screens.home.HomeState

@Composable
fun StationScreen(
    stationId: String,
    stationViewModel: StationViewModel = hiltViewModel(
        creationCallback = { factory: StationViewModel.Factory ->
            factory.create(stationId)
        }
    )
) {
    val uiState: StationState by stationViewModel.uiState.collectAsState(StationState())

    Text(uiState.name, style= MaterialTheme.typography.headlineLarge)
}