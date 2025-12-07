package mm.zamiec.garpom.ui.screens.measurement

import mm.zamiec.garpom.ui.screens.measurement.components.FireCard
import mm.zamiec.garpom.ui.screens.measurement.components.MeasurementCard
import java.time.LocalDateTime
import java.util.Date

open class MeasurementScreenState () {
    class MeasurementData(
        val stationName: String,
        val stationId: String = "",
        val date: LocalDateTime,

        val cards: List<MeasurementCard>
    ): MeasurementScreenState()

    data class Error(val message: String) : MeasurementScreenState()

    object Loading : MeasurementScreenState()

    object Deleted: MeasurementScreenState()
}

data class TriggeredAlarm(
    val alarmId: String,
    val alarmName: String,
)