package mm.zamiec.garpom.domain.model.state

import android.os.Message
import com.google.firebase.Timestamp
import javax.annotation.ParametersAreNullableByDefault

open class MeasurementScreenState () {
    class MeasurementData(
        val stationName: String,
        val stationId: String = "",
        val date: Timestamp,

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