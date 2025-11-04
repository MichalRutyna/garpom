package mm.zamiec.garpom.ui.screens.measurement

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.ui.ScaffoldElements
import mm.zamiec.garpom.ui.screens.measurement.components.MeasurementFabMenu
import mm.zamiec.garpom.ui.screens.measurement.components.MeasurementTopBar

@Composable
fun measurementScreenScaffoldElements(
    measurementId: String,
    measurementViewModel: MeasurementScreenViewModel = hiltViewModel(
        creationCallback = { factory: MeasurementScreenViewModel.Factory ->
            factory.create(measurementId)
        }
    ),
    onBack: () -> Unit,
): ScaffoldElements {
    val uiState: MeasurementScreenState by measurementViewModel.uiState.collectAsState()

    val scaffoldElements: ScaffoldElements = when (uiState) {
        is MeasurementScreenState.Loading -> ScaffoldElements()
        // TODO empty top bar?
        is MeasurementScreenState.Error -> ScaffoldElements()
        is MeasurementScreenState.MeasurementData -> {
            ScaffoldElements(
                topBar = {
                    MeasurementTopBar(onBack, uiState as MeasurementScreenState.MeasurementData)
                },
                fab = {
                    MeasurementFabMenu(
                        measurementViewModel.fabMenuExpanded.collectAsState().value,
                        measurementViewModel::toggleFabMenu
                    )
                }
            )
        }
        else -> ScaffoldElements()
    }
    return scaffoldElements
}