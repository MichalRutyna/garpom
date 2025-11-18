package mm.zamiec.garpom.ui.screens.configure

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.ui.ScaffoldElements

@Composable
fun configureScreenScaffoldElements(
    bluetoothViewModel: BluetoothViewModel = hiltViewModel(),
) : ScaffoldElements {
    val uiState by bluetoothViewModel.uiState.collectAsState()

    val scaffoldElements: ScaffoldElements =
        when (val state = uiState.screenState) {
            is ScreenState.ScanResults -> {
                ScaffoldElements(

                )
            }
            else -> ScaffoldElements()
    }
    return scaffoldElements
}