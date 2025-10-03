package mm.zamiec.garpom.ui.screens.measurement

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.domain.model.state.MeasurementScreenState
import mm.zamiec.garpom.domain.model.state.StationScreenState
import mm.zamiec.garpom.ui.screens.station.StationViewModel

@Composable
fun MeasurementScreen(
    measurementId: String,
    measurementViewModel: MeasurementScreenViewModel = hiltViewModel(
        creationCallback = { factory: MeasurementScreenViewModel.Factory ->
            factory.create(measurementId)
        }
    )
) {
    val uiState: MeasurementScreenState by measurementViewModel.uiState.collectAsState(
        MeasurementScreenState())

    Text(uiState.id, style= MaterialTheme.typography.headlineLarge)
}