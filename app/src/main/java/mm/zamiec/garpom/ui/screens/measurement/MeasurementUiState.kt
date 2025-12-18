package mm.zamiec.garpom.ui.screens.measurement

import mm.zamiec.garpom.ui.screens.measurement.components.MeasurementCard
import java.time.LocalDateTime

interface MeasurementScreenState {
    data class MeasurementData(
        val stationName: String,
        val stationId: String = "",
        val date: LocalDateTime,

        val cards: List<MeasurementCard>
    ): MeasurementScreenState

    data class Error(val message: String) : MeasurementScreenState

    data object Loading : MeasurementScreenState

    data object Deleted: MeasurementScreenState
}

data class TriggeredAlarm(
    val alarmId: String,
    val alarmName: String,
)