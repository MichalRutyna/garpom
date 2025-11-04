package mm.zamiec.garpom.ui.screens.station

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.ui.ScaffoldElements
import mm.zamiec.garpom.ui.screens.station.components.StationTopBar

@Composable
fun stationScaffoldElements(
    stationId: String,
    stationViewModel: StationViewModel = hiltViewModel(
        creationCallback = { factory: StationViewModel.Factory ->
            factory.create(stationId)
        }
    ),
    onBack: () -> Unit,
): ScaffoldElements {
    val uiState: StationScreenUiState by stationViewModel.uiState.collectAsState(StationScreenUiState())

    val scaffoldElements = when(uiState) {
        is StationScreenUiState.StationData ->
            ScaffoldElements(
                topBar = { StationTopBar(onBack, uiState as StationScreenUiState.StationData) }
            )
        is StationScreenUiState.Error -> ScaffoldElements()
        is StationScreenUiState.Loading -> ScaffoldElements()
        else -> ScaffoldElements()
    }
    return scaffoldElements
}