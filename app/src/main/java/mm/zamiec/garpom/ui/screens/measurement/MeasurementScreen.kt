package mm.zamiec.garpom.ui.screens.measurement

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.domain.model.state.MeasurementScreenState
import mm.zamiec.garpom.domain.model.state.StationScreenState
import mm.zamiec.garpom.ui.screens.auth.AuthUiState
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
    val uiState: MeasurementScreenState by measurementViewModel.uiState.collectAsState()

    when (uiState) {
        is MeasurementScreenState.Loading ->
            CircularProgressIndicator()
        is MeasurementScreenState.MeasurementData -> {
            val uiState = uiState as MeasurementScreenState.MeasurementData
            Column {
                Text(uiState.date.toString(), style=MaterialTheme.typography.headlineLarge)

                Text(uiState.temperature.toString())

                Text(uiState.triggeredAlarms.toString())
            }
        }
        is MeasurementScreenState.Error ->
            Text("Error: ${(uiState as MeasurementScreenState.Error).message}")
    }
}