package mm.zamiec.garpom.ui.screens.station

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.ehsannarmani.compose_charts.models.Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mm.zamiec.garpom.data.auth.AuthRepository
import mm.zamiec.garpom.domain.managers.GraphDataManager
import mm.zamiec.garpom.domain.managers.StationDetailsManager
import mm.zamiec.garpom.domain.model.IconType
import mm.zamiec.garpom.domain.model.Parameter

@HiltViewModel(assistedFactory = StationViewModel.Factory::class)
class StationViewModel @AssistedInject constructor(
    private val repository: AuthRepository,
    private val stationDetailsManager: StationDetailsManager,
    private val graphDataManager: GraphDataManager,
    @Assisted private val stationId: String,
) : ViewModel() {

    private val TAG = "StationViewModel"

    val uiState: Flow<StationScreenUiState> = stationDetailsManager.stationDetails(stationId)

    private val _graphData = MutableStateFlow(GraphData())
    val graphdata: StateFlow<GraphData> = _graphData

    init {
        viewModelScope.launch {
            _graphData.value = GraphData(
                graphChips = listOf(
                    ParameterChipData(
                        Parameter.TEMPERATURE,
                        graphDataManager.getTemperatureGraphLine(),
                        enabled = true
                    ),
                    ParameterChipData(
                        Parameter.AIR_HUMIDITY,
                        graphDataManager.getAirHumidityLine(),
                        enabled = false
                    ),
                )
            )
        }.invokeOnCompletion { updateGraph() }
    }

    private fun updateGraph() {
        val newLines = mutableListOf<Line>()
        newLines.addAll(
            _graphData.value.graphChips
                .filter { it.enabled }
                .map {
                    it.line
//                        .copy(strokeProgress = Animatable(0f), gradientProgress = Animatable(0f))
                }
        )
        _graphData.update {
            it.copy(lines = newLines)
        }
        Log.d(TAG, newLines.toString())
    }

    fun changeChartSelection(graphChip: ParameterChipData) {
        _graphData.update { state ->
            state.copy(
                graphChips = state.graphChips.map { chip ->
                    if (chip.parameter == graphChip.parameter) {
                        chip.copy(enabled = !chip.enabled)
                    } else chip
                }
            )
        }
        updateGraph()
    }

    @AssistedFactory
    interface Factory {
        fun create(stationId: String): StationViewModel
    }
}