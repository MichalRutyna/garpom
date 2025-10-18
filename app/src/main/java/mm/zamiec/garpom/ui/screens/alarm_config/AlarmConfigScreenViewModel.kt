package mm.zamiec.garpom.ui.screens.alarm_config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mm.zamiec.garpom.data.auth.AuthRepository
import mm.zamiec.garpom.domain.managers.AlarmConfigManager
import mm.zamiec.garpom.domain.managers.StationDetailsManager
import mm.zamiec.garpom.ui.screens.measurement.MeasurementScreenState

@HiltViewModel(assistedFactory = AlarmConfigScreenViewModel.Factory::class)
class AlarmConfigScreenViewModel @AssistedInject constructor(
    private val alarmConfigManager: AlarmConfigManager,
    @Assisted private val alarmId: String,
) : ViewModel() {

    private val TAG = "AlarmConfigScreenViewModel"

    private val _uiState = MutableStateFlow<AlarmConfigUiState>(AlarmConfigUiState.Loading)
    val uiState: StateFlow<AlarmConfigUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = AlarmConfigUiState.Loading
        viewModelScope.launch {
            _uiState.value = alarmConfigManager.alarmDetails(alarmId)
        }

    }

    @AssistedFactory
    interface Factory {
        fun create(alarmId: String): AlarmConfigScreenViewModel
    }
}