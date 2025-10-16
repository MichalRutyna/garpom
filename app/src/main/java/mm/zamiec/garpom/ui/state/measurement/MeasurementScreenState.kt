package mm.zamiec.garpom.ui.state.measurement

import java.util.Date

open class MeasurementScreenState () {
    class MeasurementData(
        val stationName: String,
        val stationId: String = "",
        val date: Date,

        val cards: List<MeasurementCard>,
        val fire: FireCard,
    ): MeasurementScreenState()

    data class Error(val message: String) : MeasurementScreenState()

    object Loading : MeasurementScreenState()
}


open class MeasurementCard(
    val title: String,
    val value: Double,
    val unit: String,
    val triggeredAlarms: List<TriggeredAlarm>
)

data class TriggeredAlarm(
    val alarmId: String,
    val alarmName: String,
)