package mm.zamiec.garpom.ui.screens.station

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _selectedGraphRange = MutableStateFlow(0f..1f)

    init {
        // loading the
        _uiState.value = StationScreenUiState.Loading
        viewModelScope.launch {
            val stationDetailsFlow = stationDetailsManager.stationDetails(stationId)
            // create a one-shot flow so we can combine, and keep listening to details
            val graphDataFlow = flow {
                emit(graphDataManager.initialGraph(stationId, PeriodSelection.LastWeek, setOf(Parameter.TEMPERATURE)))
            }

            combine(
                stationDetailsFlow,
                graphDataFlow
            ) { details, graph ->
                details to graph
            }.collect { (details, graph) ->
                _graphData.value = graph
                _uiState.value = details
            }
        }
    }

    private fun updateGraph() {
        viewModelScope.launch {
            _graphData.value = graphDataManager.updateData(stationId,_graphData.value)
        }
    }

    fun changeLineSelection(parameter: Parameter) {
        _graphData.update { state ->
            state.copy(
                enabledParameters =
                    if (state.enabledParameters.contains(parameter))
                        state.enabledParameters.minus(parameter)
                    else
                        state.enabledParameters.plus(parameter)
            )
        }
        updateGraph()
    }

    fun onRangeChangeFinished() {
        _selectedGraphRange.update { _graphData.value.graphActiveTimeRange }
        Log.d(TAG, "New range: " + _selectedGraphRange.value)
        updateGraph()
    }

    fun onChartPeriodChecked(selection: PeriodSelection) {
        _graphData.update {
            it.copy(
                selectedPeriod = selection
            )
        }
        updateGraph()
    }

    fun onRangeChange(range: ClosedFloatingPointRange<Float>) {
        _graphData.update {
            it.copy(graphActiveTimeRange = range)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(stationId: String): StationViewModel
    }
}