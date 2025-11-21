package mm.zamiec.garpom.ui.screens.station

import androidx.compose.animation.core.Animatable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.ehsannarmani.compose_charts.models.Line
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mm.zamiec.garpom.data.auth.AuthRepository
import mm.zamiec.garpom.domain.managers.GraphDataManager
import mm.zamiec.garpom.domain.managers.StationDetailsManager
import mm.zamiec.garpom.domain.model.Parameter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
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

    private val _selectedGraphRange = MutableStateFlow(0f..1f)

    init {
        // loading the
        _uiState.value = StationScreenUiState.Loading
        viewModelScope.launch {
            val stationDetailsFlow = stationDetailsManager.stationDetails(stationId)

            val graphDataFlow = flow { // create a one-shot flow so we can combine
                val measurements = allDates.size.toFloat()
                _selectedGraphRange.value = max(measurements - 7f, 0f)..measurements // default to last week
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
                    graphTimeRange = 0f..measurements,
                    graphActiveTimeRange = _selectedGraphRange.value,
                    timeRangeSteps = measurements.toInt()-1, // 0-indexed, but end-inclusive
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
        val start = _selectedGraphRange.value.start.toInt()
        val end = _selectedGraphRange.value.endInclusive.toInt()

        newLines.addAll(
            _graphData.value.graphChips
                .filter { it.enabled }
                .map {
                    it.line
                        .copy(values = it.line.values.subList(start, end))
                        .copy(strokeProgress = Animatable(0f), gradientProgress = Animatable(0f)) // reset line animation
                }
        )
        val formatter = DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault())
        _graphData.update { data ->
            data.copy(
                lines = newLines,
                graphTimeLabels = allDates
                    .subList(start, end)
                    .map {
                        it.format(formatter)
                    }
            )
        }
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
        _graphData.update {
            it.copy(graphActiveTimeRange = range)
        }
    }

    fun onRangeChangeFinished() {
        _selectedGraphRange.update { _graphData.value.graphActiveTimeRange }
        updateGraph()
    }

    @AssistedFactory
    interface Factory {
        fun create(stationId: String): StationViewModel
    }
}