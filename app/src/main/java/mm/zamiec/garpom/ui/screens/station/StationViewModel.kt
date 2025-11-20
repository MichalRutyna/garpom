package mm.zamiec.garpom.ui.screens.station

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mm.zamiec.garpom.data.auth.AuthRepository
import mm.zamiec.garpom.domain.managers.GraphDataManager
import mm.zamiec.garpom.domain.managers.StationDetailsManager
import mm.zamiec.garpom.domain.model.Parameter
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

@HiltViewModel(assistedFactory = StationViewModel.Factory::class)
class StationViewModel @AssistedInject constructor(
    private val repository: AuthRepository,
    private val stationDetailsManager: StationDetailsManager,
    private val graphDataManager: GraphDataManager,
    @Assisted private val stationId: String,
) : ViewModel() {

    private val TAG = "StationViewModel"

    private val _uiState = MutableStateFlow<StationScreenUiState>(StationScreenUiState.Loading)
    val uiState: StateFlow<StationScreenUiState> = _uiState

    private val _graphData = MutableStateFlow(GraphData())
    val graphdata: StateFlow<GraphData> = _graphData

    val allDates: List<LocalDate> = graphDataManager.getAllDates()

    init {
        // loading the
        _uiState.value = StationScreenUiState.Loading
        viewModelScope.launch {
            val stationDetailsFlow = stationDetailsManager.stationDetails(stationId)

            val graphDataFlow = flow { // create a one-shot flow so we can combine
                val days = ChronoUnit.DAYS.between(allDates.first(), allDates.last()).toFloat()
                emit(GraphData(
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
                    ),
                    graphTimeRange = 0f..days,
                    graphActiveTimeRange = max(days - 7f, 0f)..days, // default to last week
                )
                )
            }

            combine(
                stationDetailsFlow,
                graphDataFlow
            ) { details, graph ->
                details to graph
            }.collect { (details, graph) ->
                _graphData.value = graph
                _uiState.value = details

                updateGraph()
            }
        }
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

    fun changeLineSelection(graphChip: ParameterChipData) {
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
    fun onRangeChange(range: ClosedFloatingPointRange<Float>) {
        // TODO filter the graph
    }

    fun onRangeChangeFinished() {
        // TODO filter the graph
    }

    @AssistedFactory
    interface Factory {
        fun create(stationId: String): StationViewModel
    }
}