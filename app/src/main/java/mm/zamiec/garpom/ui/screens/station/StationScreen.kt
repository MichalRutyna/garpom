package mm.zamiec.garpom.ui.screens.station

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.domain.model.state.StationScreenState

@Composable
fun StationScreen(
    stationId: String,
    stationViewModel: StationViewModel = hiltViewModel(
        creationCallback = { factory: StationViewModel.Factory ->
            factory.create(stationId)
        }
    )
) {
    val uiState: StationScreenState by stationViewModel.uiState.collectAsState(StationScreenState())

    Text(uiState.name, style= MaterialTheme.typography.headlineLarge)
}