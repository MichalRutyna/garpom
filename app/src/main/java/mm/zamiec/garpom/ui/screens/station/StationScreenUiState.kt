package mm.zamiec.garpom.ui.screens.station

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import ir.ehsannarmani.compose_charts.models.Line
import mm.zamiec.garpom.domain.model.Parameter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


interface StationScreenUiState {
    data class StationData(
        val name: String = "",

        val notifications: List<NotificationItemUiState> = emptyList(),
        val measurementList: List<MeasurementSummaryItemUiState> = emptyList(),
    ) : StationScreenUiState

    data class Error(val msg: String) : StationScreenUiState

    data object Loading : StationScreenUiState
}

data class MeasurementSummaryItemUiState (
    val date: LocalDateTime = LocalDateTime.now(),
    val measurementId: String = "",
)

sealed class NotificationItemUiState(
    val message: String = "",
    val icon: ImageVector = Icons.Filled.Notifications,
    val iconDescription: String = "",
) {
    data class ErrorNotification(
        private val msg: String = "",
        val stationId: String = "",
    ) : NotificationItemUiState(msg, Icons.Filled.Warning, "An error occured")

    data class AlarmNotification(
        val measurementId: String = "",
        val measurementDate: LocalDateTime = LocalDateTime.now(),
        val alarmName: String = "",
    ) : NotificationItemUiState(
        "Alarm \"$alarmName\" went off on ${
            measurementDate.format(DateTimeFormatter.ofPattern("MMM dd"))
        }",
        Icons.Filled.Info,
        "An alarm went off") {
    }
}

data class GraphData(
    val enabledParameters: Set<Parameter> = emptySet(),
    var selectedPeriod: PeriodSelection = PeriodSelection.LastWeek,
    var storedPeriod: PeriodSelection = PeriodSelection.LastWeek, // for ui update logic
    val lines: List<Line> = emptyList(), // lines currently drawn
    val graphTimeRange: ClosedFloatingPointRange<Float> = 0f..1f, // max range
    val graphActiveTimeRange: ClosedFloatingPointRange<Float> = 0f..1f,
    val graphTimeLabels: List<String> = emptyList(),
    val timeRangeSteps: Int = 2,

)

enum class PeriodSelection(val display: String) {
    AllTime("All time"),
    LastWeek("Last week"),
    Last3Days("Last 3 days"),
    Last24("Last 24h");
}
