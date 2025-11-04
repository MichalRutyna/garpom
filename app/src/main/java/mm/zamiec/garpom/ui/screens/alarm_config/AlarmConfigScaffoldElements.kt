package mm.zamiec.garpom.ui.screens.alarm_config

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.ui.ScaffoldElements
import mm.zamiec.garpom.ui.screens.alarm_config.components.AlarmConfigTitleBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun alarmConfigScaffoldElements(
    alarmId: String,
    alarmConfigViewModel: AlarmConfigScreenViewModel = hiltViewModel(
        creationCallback = { factory: AlarmConfigScreenViewModel.Factory ->
            factory.create(alarmId)
        }
    ),
    onBack: () -> Unit
): ScaffoldElements {
    val uiState by alarmConfigViewModel.uiState.collectAsState()

    val scaffoldElements: ScaffoldElements = when (val state = uiState) {
        is AlarmConfigUiState.Loading -> ScaffoldElements()
        is AlarmConfigUiState.Error -> ScaffoldElements()
        is AlarmConfigUiState.ConfigData -> {
            ScaffoldElements(
                topBar = {
                    AlarmConfigTitleBar(onBack, state.alarmName)
                },
                fab = {
                    FloatingActionButton(onClick = alarmConfigViewModel::saveStates) {
                        Icon(Icons.Filled.Done, contentDescription = "Save the alarm.")
                    }
                }
            )
        }
        else -> ScaffoldElements()
    }

    return scaffoldElements

}

//onSave: (Boolean, String, String, TimePickerState, TimePickerState, SnapshotStateList<StationChoice>,
//Map<String, MutableState<ClosedFloatingPointRange<Float>>>) -> Unit,